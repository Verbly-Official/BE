package verbly.spring.domain.review.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.library.entity.LibraryItem;
import verbly.spring.domain.library.enums.LibraryItemStatus;
import verbly.spring.domain.library.repository.LibraryItemRepository;
import verbly.spring.domain.review.dto.request.QuizAnswerSubmitRequest;
import verbly.spring.domain.review.dto.response.*;
import verbly.spring.domain.review.entity.*;
import verbly.spring.domain.review.enums.ReviewSessionStatus;
import verbly.spring.domain.review.enums.ReviewTaskStatus;
import verbly.spring.domain.review.enums.ReviewQuestionType;
import verbly.spring.domain.review.exception.ReviewErrorStatus;
import verbly.spring.domain.review.repository.*;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.common.exception.BaseException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizServiceImpl implements QuizService {

    private final ReviewSessionRepository reviewSessionRepository;
    private final ReviewTaskRepository reviewTaskRepository;
    private final ReviewQuestionRepository reviewQuestionRepository;
    private final ReviewAnswerRepository reviewAnswerRepository;

    private final LibraryItemRepository libraryItemRepository;
    private final ObjectMapper objectMapper;

    @Override
    public QuizStartResponse startSession(Long userId) {
        // 1) 진행중 세션이 있으면 “재진입 불가 정책” → 자동 quit + 롤백
        reviewSessionRepository.findTopByUserIdAndStatusOrderByCreatedAtDesc(userId, ReviewSessionStatus.IN_PROGRESS)
                .ifPresent(s -> quitInternal(userId, s.getId()));

        // 2) 모든 라이브러리 아이템에 대해 review_task가 없으면 생성 (lazy 보정)
        ensureReviewTasksExist(userId);

        // 3) 퀴즈 개수 = PENDING 전체
        List<ReviewTask> pendingTasks = reviewTaskRepository
                .findAllByUserIdAndStatusOrderByCreatedAtAscIdAsc(userId, ReviewTaskStatus.PENDING);

        if (pendingTasks.isEmpty()) {
            throw new BaseException(ReviewErrorStatus.QUIZ_NO_PENDING_ITEMS);
        }

        // 4) 세션 생성
        ReviewSession session = ReviewSession.start(userId, pendingTasks.size());
        session = reviewSessionRepository.save(session);

        // 5) task 전부 세션에 claim (처음부터 끝까지 다 풀어야 제출 성립)
        LocalDateTime now = LocalDateTime.now();
        int order = 1;
        for (ReviewTask t : pendingTasks) {
            t.claim(session.getId(), order++, now);
        }
        reviewTaskRepository.saveAll(pendingTasks);

        // 6) 문제 생성(여기서는 “기본 생성기”로 단순 생성)
        List<ReviewQuestion> questions = generateQuestions(userId, pendingTasks);
        reviewQuestionRepository.saveAll(questions);

        // 7) 첫 문제 반환
        ReviewQuestion first = findFirstQuestionOrThrow(session.getId(), 1);
        QuizQuestionResponse firstDto = toQuestionResponse(first);

        return new QuizStartResponse(
                session.getId(),
                session.getTotalTasks(),
                session.getCurrentIndex(),
                firstDto,
                session.getCreatedAt()
        );
    }

    @Override
    public QuizHintResponse useHint(Long userId, Long sessionId, Long questionId) {
        ReviewSession session = reviewSessionRepository.findByIdAndUserIdForUpdate(sessionId, userId)
                .orElseThrow(() -> new BaseException(ReviewErrorStatus.QUIZ_SESSION_NOT_FOUND));

        if (session.getStatus() != ReviewSessionStatus.IN_PROGRESS) {
            throw new BaseException(ReviewErrorStatus.QUIZ_SESSION_NOT_IN_PROGRESS);
        }

        ReviewQuestion q = reviewQuestionRepository.findWithTaskAndItemById(questionId)
                .orElseThrow(() -> new BaseException(ReviewErrorStatus.QUIZ_QUESTION_NOT_FOUND));

        validateQuestionOwnership(userId, sessionId, q);

        if (q.getHintUsed() >= q.getHintTotal()) {
            throw new BaseException(ReviewErrorStatus.QUIZ_NO_HINTS_REMAINING);
        }

        q.useHint();

        return new QuizHintResponse(
                q.getId(),
                q.getHintUsed(),
                q.getHintTotal(),
                Math.max(0, q.getHintTotal() - q.getHintUsed())
        );
    }

    @Override
    public QuizAnswerSubmitResponse submitAnswer(Long userId, Long sessionId, Long questionId, QuizAnswerSubmitRequest request) {
        ReviewSession session = reviewSessionRepository.findByIdAndUserIdForUpdate(sessionId, userId)
                .orElseThrow(() -> new BaseException(ReviewErrorStatus.QUIZ_SESSION_NOT_FOUND));

        if (session.getStatus() != ReviewSessionStatus.IN_PROGRESS) {
            throw new BaseException(ReviewErrorStatus.QUIZ_SESSION_NOT_IN_PROGRESS);
        }

        ReviewQuestion q = reviewQuestionRepository.findWithTaskAndItemById(questionId)
                .orElseThrow(() -> new BaseException(ReviewErrorStatus.QUIZ_QUESTION_NOT_FOUND));

        validateQuestionOwnership(userId, sessionId, q);

        // “순서대로만 풀기” 정책 (스킵 방지)
        Integer taskOrder = q.getReviewTask().getTaskOrder();
        if (taskOrder == null || taskOrder != session.getCurrentIndex()) {
            throw new BaseException(ReviewErrorStatus.QUIZ_OUT_OF_ORDER);
        }

        // attempt_no 계산
        int nextAttempt = reviewAnswerRepository.findTopByReviewQuestion_IdOrderByAttemptNoDesc(q.getId())
                .map(a -> a.getAttemptNo() + 1)
                .orElse(1);

        boolean correct = grade(q, request.userAnswerJson());

        ReviewAnswer answer = ReviewAnswer.of(
                q,
                nextAttempt,
                request.userAnswerJson(),
                correct,
                request.mistakeNote()
        );
        reviewAnswerRepository.save(answer);

        // 마지막 문제면 = “제출 완료”로 간주 (부분 제출 없음)
        boolean isLast = (session.getCurrentIndex() >= session.getTotalTasks());
        LocalDateTime now = LocalDateTime.now();

        if (isLast) {
            completeSessionAndTasks(sessionId, userId, now);

            return new QuizAnswerSubmitResponse(
                    q.getId(),
                    correct,
                    q.getAnswerKeyJson(),
                    q.getExplanation(),
                    session.getTotalTasks(),
                    session.getTotalTasks(),
                    true,
                    null
            );
        }

        // 다음 문제로 이동
        session.advance();

        ReviewQuestion nextQ = findFirstQuestionOrThrow(sessionId, session.getCurrentIndex());
        QuizQuestionResponse nextDto = toQuestionResponse(nextQ);

        return new QuizAnswerSubmitResponse(
                q.getId(),
                correct,
                q.getAnswerKeyJson(),
                q.getExplanation(),
                session.getCurrentIndex(),
                session.getTotalTasks(),
                false,
                nextDto
        );
    }

    @Override
    public QuizQuitResponse quit(Long userId, Long sessionId) {
        quitInternal(userId, sessionId);
        return new QuizQuitResponse(sessionId, "QUIT");
    }

    @Override
    @Transactional(readOnly = true)
    public QuizResultResponse getResult(Long userId, Long sessionId) {
        ReviewSession session = reviewSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new BaseException(ReviewErrorStatus.QUIZ_SESSION_NOT_FOUND));

        // 결과는 완료/중단 세션 모두 조회 가능하게 할 수도 있지만,
        // 화면상 보통 "완료 후 결과"가 자연스러움
        if (session.getStatus() == ReviewSessionStatus.IN_PROGRESS) {
            throw new BaseException(ReviewErrorStatus.QUIZ_SESSION_NOT_IN_PROGRESS);
        }

        List<ReviewTask> tasks = reviewTaskRepository.findAllBySessionIdOrderByTaskOrderAsc(sessionId);
        List<Long> taskIds = tasks.stream().map(ReviewTask::getId).toList();

        List<ReviewQuestion> questions = reviewQuestionRepository.findAllByReviewTask_IdIn(taskIds);
        if (questions.isEmpty()) {
            // quit 후 문제를 삭제해버린 케이스면 결과가 없을 수 있음
            return new QuizResultResponse(sessionId, 0, 0, 0, 0, List.of());
        }

        List<Long> questionIds = questions.stream().map(ReviewQuestion::getId).toList();

        // 각 question의 마지막 답만 뽑기 (쿼리 최소화: 전체 answers 한번에 가져와서 메모리에서 그룹)
        List<ReviewAnswer> answers = reviewAnswerRepository
                .findAllByReviewQuestion_IdInOrderByReviewQuestion_IdAscAttemptNoDesc(questionIds);

        Map<Long, ReviewAnswer> lastAnswerByQuestion = new LinkedHashMap<>();
        for (ReviewAnswer a : answers) {
            Long qid = a.getReviewQuestion().getId();
            lastAnswerByQuestion.putIfAbsent(qid, a);
        }

        int total = questions.size();
        int correctCount = 0;

        Map<Long, ReviewQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(ReviewQuestion::getId, q -> q));

        List<QuizMistakeResponse> mistakes = new ArrayList<>();

        for (ReviewQuestion q : questions) {
            ReviewAnswer last = lastAnswerByQuestion.get(q.getId());
            if (last != null && last.isCorrect()) {
                correctCount++;
            } else {
                JsonNode userAnswer = (last == null) ? null : last.getUserAnswerJson();
                mistakes.add(new QuizMistakeResponse(
                        q.getId(),
                        q.getLibraryItem().getId(),
                        q.getLibraryItem().getPhrase(),
                        q.getPrompt(),
                        userAnswer,
                        q.getAnswerKeyJson(),
                        q.getExplanation()
                ));
            }
        }

        int wrongCount = total - correctCount;
        int accuracy = (total == 0) ? 0 : (int) Math.round((correctCount * 100.0) / total);

        return new QuizResultResponse(
                sessionId,
                total,
                correctCount,
                wrongCount,
                accuracy,
                mistakes
        );
    }

    @Override
    public QuizStartResponse retryMistakes(Long userId, Long sessionId) {
        ReviewSession session = reviewSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new BaseException(ReviewErrorStatus.QUIZ_SESSION_NOT_FOUND));

        if (session.getStatus() == ReviewSessionStatus.IN_PROGRESS) {
            throw new BaseException(ReviewErrorStatus.QUIZ_SESSION_NOT_IN_PROGRESS);
        }

        // 기존 세션에서 오답만 추출
        List<ReviewTask> tasks = reviewTaskRepository.findAllBySessionIdOrderByTaskOrderAsc(sessionId);
        List<Long> taskIds = tasks.stream().map(ReviewTask::getId).toList();

        List<ReviewQuestion> questions = reviewQuestionRepository.findAllByReviewTask_IdIn(taskIds);
        List<Long> questionIds = questions.stream().map(ReviewQuestion::getId).toList();

        List<ReviewAnswer> answers = reviewAnswerRepository
                .findAllByReviewQuestion_IdInOrderByReviewQuestion_IdAscAttemptNoDesc(questionIds);

        Map<Long, ReviewAnswer> lastAnswerByQuestion = new LinkedHashMap<>();
        for (ReviewAnswer a : answers) {
            lastAnswerByQuestion.putIfAbsent(a.getReviewQuestion().getId(), a);
        }

        // 오답 question → 해당 task 추출
        Set<Long> wrongTaskIds = new LinkedHashSet<>();
        for (ReviewQuestion q : questions) {
            ReviewAnswer last = lastAnswerByQuestion.get(q.getId());
            if (last == null || !last.isCorrect()) {
                wrongTaskIds.add(q.getReviewTask().getId());
            }
        }

        if (wrongTaskIds.isEmpty()) {
            throw new BaseException(ReviewErrorStatus.QUIZ_NO_MISTAKES);
        }

        // 오답 task들만 PENDING으로 되돌리고, 기존 문제는 삭제(재출제)
        List<ReviewTask> wrongTasks = tasks.stream()
                .filter(t -> wrongTaskIds.contains(t.getId()))
                .toList();

        for (ReviewTask t : wrongTasks) {
            t.rollbackToPending();
        }
        reviewTaskRepository.saveAll(wrongTasks);

        reviewQuestionRepository.deleteAllByReviewTask_IdIn(new ArrayList<>(wrongTaskIds));

        // “오답만” 새 세션 시작
        return startSessionOnlyWithGivenPendingTasks(userId, wrongTasks);
    }

    // =========================
    // internal helpers
    // =========================

    private void quitInternal(Long userId, Long sessionId) {
        ReviewSession session = reviewSessionRepository.findByIdAndUserIdForUpdate(sessionId, userId)
                .orElseThrow(() -> new BaseException(ReviewErrorStatus.QUIZ_SESSION_NOT_FOUND));

        if (session.getStatus() != ReviewSessionStatus.IN_PROGRESS) {
            // 이미 끝났으면 그냥 종료 처리만
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        session.quit(now);

        // 세션에 붙은 task 전부 롤백 (처음부터 다시 정책)
        List<ReviewTask> tasks = reviewTaskRepository.findAllBySessionId(sessionId);
        List<Long> taskIds = tasks.stream().map(ReviewTask::getId).toList();

        for (ReviewTask t : tasks) {
            t.rollbackToPending();
        }
        reviewTaskRepository.saveAll(tasks);

        // 문제/답 기록은 “이번 시도”는 의미 없으니 삭제(선택)
        reviewQuestionRepository.deleteAllByReviewTask_IdIn(taskIds);
    }

    private void completeSessionAndTasks(Long sessionId, Long userId, LocalDateTime now) {
        ReviewSession session = reviewSessionRepository.findByIdAndUserIdForUpdate(sessionId, userId)
                .orElseThrow(() -> new BaseException(ReviewErrorStatus.QUIZ_SESSION_NOT_FOUND));

        List<ReviewTask> tasks = reviewTaskRepository.findAllBySessionIdOrderByTaskOrderAsc(sessionId);

        // “끝까지 다 풀어야 제출” → 여기서 한 번에 COMPLETED 처리
        for (ReviewTask t : tasks) {
            t.complete(now);
        }
        reviewTaskRepository.saveAll(tasks);

        session.complete(now);
    }

    private void validateQuestionOwnership(Long userId, Long sessionId, ReviewQuestion q) {
        ReviewTask task = q.getReviewTask();
        if (!Objects.equals(task.getUserId(), userId) || !Objects.equals(task.getSessionId(), sessionId)) {
            throw new BaseException(ReviewErrorStatus.QUIZ_FORBIDDEN);
        }
    }

    private ReviewQuestion findFirstQuestionOrThrow(Long sessionId, int taskOrder) {
        ReviewTask task = reviewTaskRepository.findBySessionIdAndTaskOrder(sessionId, taskOrder)
                .orElseThrow(() -> new BaseException(ReviewErrorStatus.QUIZ_SESSION_NOT_FOUND));

        return reviewQuestionRepository.findTopByReviewTask_IdOrderByQuestionOrderAsc(task.getId())
                .orElseThrow(() -> new BaseException(ReviewErrorStatus.QUIZ_QUESTION_NOT_FOUND));
    }

    private QuizQuestionResponse toQuestionResponse(ReviewQuestion q) {
        List<String> options = new ArrayList<>();
        if (q.getOptionsJson() != null && q.getOptionsJson().isArray()) {
            for (JsonNode node : q.getOptionsJson()) {
                options.add(node.asText());
            }
        }
        return new QuizQuestionResponse(
                q.getId(),
                q.getLibraryItem().getId(),
                q.getLibraryItem().getPhrase(),
                q.getQuestionType().name(),
                q.getPrompt(),
                options,
                q.getHintTotal(),
                q.getHintUsed()
        );
    }

    private boolean grade(ReviewQuestion q, JsonNode userAnswerJson) {
        // 기본 규칙:
        // - answer_key_json: { "answer": "정답" } 형태라고 가정
        // - user_answer_json: { "answer": "사용자답" } 형태라고 가정
        JsonNode key = q.getAnswerKeyJson();
        if (key == null || key.get("answer") == null || userAnswerJson == null || userAnswerJson.get("answer") == null) {
            return false;
        }
        String expected = key.get("answer").asText();
        String actual = userAnswerJson.get("answer").asText();
        return Objects.equals(normalize(expected), normalize(actual));
    }

    private String normalize(String s) {
        if (s == null) return null;
        return s.trim().toLowerCase(Locale.ROOT);
    }

    private void ensureReviewTasksExist(Long userId) {
        // 라이브러리 ACTIVE 전체에 대해 task 없으면 생성
        List<LibraryItem> items = libraryItemRepository.findAllByUserIdAndStatus(userId, LibraryItemStatus.ACTIVE);
        List<ReviewTask> existingTasks = reviewTaskRepository.findAllByUserId(userId);

        Set<Long> existingItemIds = existingTasks.stream()
                .map(ReviewTask::getLibraryItemId)
                .collect(Collectors.toSet());

        List<ReviewTask> toCreate = new ArrayList<>();
        for (LibraryItem item : items) {
            if (!existingItemIds.contains(item.getId())) {
                toCreate.add(ReviewTask.pending(userId, item.getId()));
            }
        }
        if (!toCreate.isEmpty()) {
            reviewTaskRepository.saveAll(toCreate);
        }
    }

    private List<ReviewQuestion> generateQuestions(Long userId, List<ReviewTask> tasks) {
        // NOTE: 실제 서비스에서는 AI 생성/코퍼스 생성으로 대체 가능
        // 지금은 동작 가능한 최소 구현

        List<ReviewQuestion> result = new ArrayList<>();

        for (ReviewTask t : tasks) {
            LibraryItem item = libraryItemRepository.findById(t.getLibraryItemId())
                    .orElseThrow(() -> new BaseException(ErrorStatus._BAD_REQUEST));

            // 단순 MCQ: "의미(ko)"를 맞히게 함
            String correct = (item.getMeaningKo() != null && !item.getMeaningKo().isBlank())
                    ? item.getMeaningKo()
                    : "(meaning not set)";

            ObjectNode answerKey = objectMapper.createObjectNode().put("answer", correct);

            ArrayNode options = objectMapper.createArrayNode();
            options.add(correct);

            // 보기 더 채우기(간단히 더미)
            options.add("dummy option 1");
            options.add("dummy option 2");
            options.add("dummy option 3");

            ReviewQuestion q = ReviewQuestion.of(
                    t,
                    item,
                    1,
                    ReviewQuestionType.mcq,
                    "다음 표현의 의미(한국어)를 고르세요: " + item.getPhrase(),
                    options,
                    answerKey
            );

            result.add(q);
        }

        return result;
    }

    private QuizStartResponse startSessionOnlyWithGivenPendingTasks(Long userId, List<ReviewTask> pendingTasks) {
        if (pendingTasks == null || pendingTasks.isEmpty()) {
            throw new BaseException(ReviewErrorStatus.QUIZ_NO_MISTAKES);
        }

        ReviewSession session = ReviewSession.start(userId, pendingTasks.size());
        session = reviewSessionRepository.save(session);

        LocalDateTime now = LocalDateTime.now();
        int order = 1;
        for (ReviewTask t : pendingTasks) {
            t.claim(session.getId(), order++, now);
        }
        reviewTaskRepository.saveAll(pendingTasks);

        List<ReviewQuestion> questions = generateQuestions(userId, pendingTasks);
        reviewQuestionRepository.saveAll(questions);

        ReviewQuestion first = findFirstQuestionOrThrow(session.getId(), 1);

        return new QuizStartResponse(
                session.getId(),
                session.getTotalTasks(),
                session.getCurrentIndex(),
                toQuestionResponse(first),
                session.getCreatedAt()
        );
    }
}
