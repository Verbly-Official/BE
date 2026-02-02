package verbly.spring.domain.quizreview.service;

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
import verbly.spring.domain.quizreview.converter.ReviewConverter;
import verbly.spring.domain.quizreview.dto.request.ReviewRequestDTO;
import verbly.spring.domain.quizreview.dto.response.ReviewResponseDTO;
import verbly.spring.domain.quizreview.entity.ReviewAnswer;
import verbly.spring.domain.quizreview.entity.ReviewQuestion;
import verbly.spring.domain.quizreview.entity.ReviewSession;
import verbly.spring.domain.quizreview.entity.ReviewTask;
import verbly.spring.domain.quizreview.enums.ReviewQuestionType;
import verbly.spring.domain.quizreview.enums.ReviewSessionStatus;
import verbly.spring.domain.quizreview.enums.ReviewTaskStatus;
import verbly.spring.domain.quizreview.exception.ReviewHandler;
import verbly.spring.domain.quizreview.repository.ReviewAnswerRepository;
import verbly.spring.domain.quizreview.repository.ReviewQuestionRepository;
import verbly.spring.domain.quizreview.repository.ReviewSessionRepository;
import verbly.spring.domain.quizreview.repository.ReviewTaskRepository;
import verbly.spring.domain.quizreview.validate.ReviewValidator;
import verbly.spring.global.common.code.ErrorStatus;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewCommandServiceImpl implements ReviewCommandService {

    private final ReviewSessionRepository reviewSessionRepository;
    private final ReviewTaskRepository reviewTaskRepository;
    private final ReviewQuestionRepository reviewQuestionRepository;
    private final ReviewAnswerRepository reviewAnswerRepository;

    private final LibraryItemRepository libraryItemRepository;
    private final ObjectMapper objectMapper;
    private final ReviewValidator reviewValidator;

    @Override
    public ReviewResponseDTO.QuizStartResponse startSession(Long userId) {
        // 1) 진행중 세션이 있으면 “재진입 불가” 정책: 자동 quit + 롤백
        reviewSessionRepository.findTopByUserIdAndStatusOrderByCreatedAtDesc(userId, ReviewSessionStatus.IN_PROGRESS)
                .ifPresent(s -> quitInternal(userId, s.getId()));

        // 2) 모든 라이브러리 아이템에 대해 review_task가 없으면 생성 (lazy 보정)
        ensureReviewTasksExist(userId);

        // 3) 퀴즈 개수 = PENDING 전체
        List<ReviewTask> pendingTasks = reviewTaskRepository
                .findAllByUserIdAndStatusOrderByCreatedAtAscIdAsc(userId, ReviewTaskStatus.PENDING);

        if (pendingTasks.isEmpty()) {
            throw new ReviewHandler(ErrorStatus.QUIZ_NO_PENDING_ITEMS);
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

        // 6) 문제 생성(현재는 최소 구현: 의미(ko) 맞히는 MCQ)
        List<ReviewQuestion> questions = generateQuestions(userId, pendingTasks);
        reviewQuestionRepository.saveAll(questions);

        // 7) 첫 문제 반환
        ReviewQuestion first = findFirstQuestionOrThrow(session.getId(), 1);

        return new ReviewResponseDTO.QuizStartResponse(
                session.getId(),
                session.getTotalTasks(),
                session.getCurrentIndex(),
                ReviewConverter.toQuestionResponse(first),
                session.getCreatedAt()
        );
    }

    public ReviewResponseDTO.QuizHintResponse useHint(Long userId, Long sessionId, Long questionId) {
        ReviewSession session = reviewValidator.validateOwnedSessionForUpdate(userId, sessionId);
        reviewValidator.validateSessionInProgress(session);

        ReviewQuestion q = reviewValidator.validateQuestionWithTaskAndItem(questionId);
        reviewValidator.validateQuestionOwnership(userId, sessionId, q);
        // ✅ 세션 전체 힌트 제한 체크
        if (session.getHintUsed() >= session.getHintTotal()) {
            throw new ReviewHandler(ErrorStatus.QUIZ_NO_HINTS_REMAINING);
        }

        // ✅ 세션 힌트 사용 1회 차감
        session.useHint();
        String textHint = q.getHint();

        return new ReviewResponseDTO.QuizHintResponse(
                q.getId(),
                session.getHintUsed(),   // 이제 “세션 기준”으로 내려주는 게 더 자연스러움
                session.getHintTotal(),
                Math.max(0, session.getHintTotal() - session.getHintUsed()),
                textHint
        );
    }

    @Override
    public ReviewResponseDTO.QuizAnswerSubmitResponse submitAnswer(
            Long userId,
            Long sessionId,
            Long questionId,
            ReviewRequestDTO.QuizAnswerSubmitRequest request
    ) {
        ReviewSession session = reviewValidator.validateOwnedSessionForUpdate(userId, sessionId);
        reviewValidator.validateSessionInProgress(session);

        ReviewQuestion q = reviewValidator.validateQuestionWithTaskAndItem(questionId);
        reviewValidator.validateQuestionOwnership(userId, sessionId, q);

        // “순서대로만 풀기” 정책 (스킵 방지)
        Integer taskOrder = q.getReviewTask().getTaskOrder();
        if (taskOrder == null || taskOrder != session.getCurrentIndex()) {
            throw new ReviewHandler(ErrorStatus.QUIZ_OUT_OF_ORDER);
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
                correct
        );
        reviewAnswerRepository.save(answer);

        boolean isLast = (session.getCurrentIndex() >= session.getTotalTasks());
        LocalDateTime now = LocalDateTime.now();

        if (isLast) {
            completeSessionAndTasks(sessionId, userId, now);

            return new ReviewResponseDTO.QuizAnswerSubmitResponse(
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

        return new ReviewResponseDTO.QuizAnswerSubmitResponse(
                q.getId(),
                correct,
                q.getAnswerKeyJson(),
                q.getExplanation(),
                session.getCurrentIndex(),
                session.getTotalTasks(),
                false,
                ReviewConverter.toQuestionResponse(nextQ)
        );
    }

    @Override
    public ReviewResponseDTO.QuizQuitResponse quit(Long userId, Long sessionId) {
        quitInternal(userId, sessionId);
        return new ReviewResponseDTO.QuizQuitResponse(sessionId, "QUIT");
    }

    @Override
    public ReviewResponseDTO.QuizStartResponse retryMistakes(Long userId, Long sessionId) {
        ReviewSession session = reviewValidator.validateOwnedSession(userId, sessionId);

        if (session.getStatus() == ReviewSessionStatus.IN_PROGRESS) {
            throw new ReviewHandler(ErrorStatus.QUIZ_SESSION_NOT_IN_PROGRESS);
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
            throw new ReviewHandler(ErrorStatus.QUIZ_NO_MISTAKES);
        }

        // 오답 task들만 PENDING으로 되돌리고, 기존 문제는 삭제(재출제)
        List<ReviewTask> wrongTasks = tasks.stream()
                .filter(t -> wrongTaskIds.contains(t.getId()))
                .toList();

        // 오답 task들만 PENDING으로 되돌리고
        for (ReviewTask t : wrongTasks) {
            t.rollbackToPending();
        }
        reviewTaskRepository.saveAll(wrongTasks);

    // 오답 task에 해당하는 questionIds 추출
        List<Long> wrongQuestionIds = questions.stream()
                .filter(q -> wrongTaskIds.contains(q.getReviewTask().getId()))
                .map(ReviewQuestion::getId)
                .toList();


        if (!wrongQuestionIds.isEmpty()) {
            reviewAnswerRepository.deleteAllByReviewQuestion_IdIn(wrongQuestionIds);
            reviewAnswerRepository.flush(); // 삭제 SQL 먼저 반영
        }

        reviewQuestionRepository.deleteAllByReviewTask_IdIn(new ArrayList<>(wrongTaskIds));
        reviewQuestionRepository.flush(); // 삭제 SQL 먼저 반영

        return startSessionOnlyWithGivenPendingTasks(userId, wrongTasks);
    }

    // =========================
    // internal helpers
    // =========================

    private void quitInternal(Long userId, Long sessionId) {
        ReviewSession session = reviewValidator.validateOwnedSessionForUpdate(userId, sessionId);

        if (session.getStatus() != ReviewSessionStatus.IN_PROGRESS) {
            // 이미 끝났으면 종료 처리만
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

        // 해당 task들의 questionId 수집
        List<Long> questionIds = reviewQuestionRepository.findAllByReviewTask_IdIn(taskIds)
                .stream()
                .map(ReviewQuestion::getId)
                .toList();


        if (!questionIds.isEmpty()) {
            reviewAnswerRepository.deleteAllByReviewQuestion_IdIn(questionIds);
        }


        reviewQuestionRepository.deleteAllByReviewTask_IdIn(taskIds);
    }

    private void completeSessionAndTasks(Long sessionId, Long userId, LocalDateTime now) {
        ReviewSession session = reviewValidator.validateOwnedSessionForUpdate(userId, sessionId);

        List<ReviewTask> tasks = reviewTaskRepository.findAllBySessionIdOrderByTaskOrderAsc(sessionId);

        // “끝까지 다 풀어야 제출” → 여기서 한 번에 COMPLETED 처리
        for (ReviewTask t : tasks) {
            t.complete(now);
        }
        reviewTaskRepository.saveAll(tasks);

        session.complete(now);
    }

    private ReviewQuestion findFirstQuestionOrThrow(Long sessionId, int taskOrder) {
        ReviewTask task = reviewValidator.validateTaskByOrder(sessionId, taskOrder);

        return reviewQuestionRepository.findTopByReviewTask_IdOrderByQuestionOrderAsc(task.getId())
                .orElseThrow(() -> new ReviewHandler(ErrorStatus.QUIZ_QUESTION_NOT_FOUND));
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


        List<ReviewQuestion> result = new ArrayList<>();

        for (ReviewTask t : tasks) {
            LibraryItem item = libraryItemRepository.findById(t.getLibraryItemId())
                    .orElseThrow(() -> new ReviewHandler(ErrorStatus._BAD_REQUEST));

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

    private ReviewResponseDTO.QuizStartResponse startSessionOnlyWithGivenPendingTasks(Long userId, List<ReviewTask> pendingTasks) {
        if (pendingTasks == null || pendingTasks.isEmpty()) {
            throw new ReviewHandler(ErrorStatus.QUIZ_NO_MISTAKES);
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

        return new ReviewResponseDTO.QuizStartResponse(
                session.getId(),
                session.getTotalTasks(),
                session.getCurrentIndex(),
                ReviewConverter.toQuestionResponse(first),
                session.getCreatedAt()
        );
    }
}
