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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        if (tasks == null || tasks.isEmpty()) return List.of();

        // 1) 아이템 로드 (task에 해당하는 libraryItem들)
        Set<Long> itemIds = tasks.stream()
                .map(ReviewTask::getLibraryItemId)
                .collect(Collectors.toSet());

        Map<Long, LibraryItem> itemById = libraryItemRepository.findAllById(itemIds).stream()
                .collect(Collectors.toMap(LibraryItem::getId, it -> it));

        // 2) 객관식 오답 풀: 유저 라이브러리에서 phrase 후보 뽑기
        List<LibraryItem> distractorPool =
                libraryItemRepository.findAllByUserIdAndStatus(userId, LibraryItemStatus.ACTIVE);

        // 3) 50:50 랜덤 배정 (task당 1문항 유지)
        int total = tasks.size();
        int clozeCount = total / 2;
        Set<Long> clozeTaskIds = pickRandomTaskIds(tasks, clozeCount);

        List<ReviewQuestion> result = new ArrayList<>(total);

        for (ReviewTask task : tasks) {
            LibraryItem item = itemById.get(task.getLibraryItemId());
            if (item == null) throw new ReviewHandler(ErrorStatus._BAD_REQUEST);

            String phrase = safe(item.getPhrase());
            if (phrase.isBlank()) throw new ReviewHandler(ErrorStatus._BAD_REQUEST);

            ReviewQuestionType type = clozeTaskIds.contains(task.getId())
                    ? ReviewQuestionType.cloze
                    : ReviewQuestionType.mcq;

            // ✅ 항상 예문 기반 (없거나 빈칸 못 뚫으면 더미 예문 생성)
            ExamplePack ex = pickExampleOrDummy(item, phrase);

            // ✅ 예문에서 phrase를 ____로 치환
            String blanked = blankOutFirstOccurrenceFlexible(ex.exampleEn(), phrase);

            // ✅ 정답은 phrase
            ObjectNode answerKey = objectMapper.createObjectNode().put("answer", phrase);

            // ✅ 힌트는 "예문 뜻"만
            String hint = !safe(ex.exampleKo()).isBlank()
                    ? safe(ex.exampleKo())
                    : safe(item.getMeaningKo()); // fallback

            String prompt;
            JsonNode optionsJson = null;

            if (type == ReviewQuestionType.cloze) {
                prompt = "빈칸(____)에 들어갈 구문을 입력하세요:\n" + blanked;
            } else {
                List<String> options = buildMcqPhraseOptions(distractorPool, item.getId(), phrase, 3);
                ArrayNode arr = objectMapper.createArrayNode();
                options.forEach(arr::add);
                optionsJson = arr;

                prompt = "빈칸(____)에 들어갈 구문을 고르세요:\n" + blanked;
            }

            ReviewQuestion q = ReviewQuestion.of(
                    task,
                    item,
                    1,
                    type,
                    prompt,
                    optionsJson,
                    answerKey
            );

            // ✅ hint 세팅 (setter 있으면 setter, 없으면 reflection)
            applyHint(q, hint);

            result.add(q);
        }

        return result;
    }

// -------- helpers --------

    private record ExamplePack(String exampleEn, String exampleKo) {}

    private ExamplePack pickExampleOrDummy(LibraryItem item, String phrase) {
        List<ExamplePack> packs = new ArrayList<>();

        if (item.getExamples() != null) {
            for (var ex : item.getExamples()) {
                String en = ex.getExampleEn();
                if (en != null && !en.isBlank()) {
                    packs.add(new ExamplePack(en.trim(), ex.getExampleKo()));
                }
            }
        }

        // phrase 포함 예문 우선
        List<ExamplePack> contains = packs.stream()
                .filter(p -> containsFlexible(p.exampleEn(), phrase))
                .toList();

        if (!contains.isEmpty()) {
            return contains.get(ThreadLocalRandom.current().nextInt(contains.size()));
        }

        // 예문은 있는데 phrase가 포함된 예문이 하나도 없으면 → 더미 예문으로 (빈칸 출제를 보장)
        if (!packs.isEmpty()) {
            // 여기서 "그냥 첫 예문을 쓰되 앞에 ____ 붙이기" 같은 정책도 가능하지만,
            // 요구사항이 '해당 구문을 빈칸' 이라 더미로 안전하게 처리.
        }

        // 예문이 아예 없을 때만 더미
        String dummyEn = "I learned the expression \"" + phrase + "\" today.";
        String dummyKo = "나는 오늘 \"" + phrase + "\"라는 표현을 배웠다.";
        return new ExamplePack(dummyEn, dummyKo);
    }

    private boolean containsFlexible(String text, String phrase) {
        if (text == null || phrase == null) return false;
        String p = phrase.trim();
        if (p.isEmpty()) return false;
        Pattern pat = Pattern.compile(buildFlexiblePhraseRegex(p), Pattern.CASE_INSENSITIVE);
        return pat.matcher(text).find();
    }

    private String blankOutFirstOccurrenceFlexible(String exampleEn, String phrase) {
        if (exampleEn == null || exampleEn.isBlank()) return "____";
        if (phrase == null || phrase.isBlank()) return "____";

        Pattern pat = Pattern.compile(buildFlexiblePhraseRegex(phrase.trim()), Pattern.CASE_INSENSITIVE);
        Matcher m = pat.matcher(exampleEn);

        if (!m.find()) {
            // phrase 못 찾으면 더미 예문을 선택했어야 하지만,
            // 안전장치: 그래도 예문 형태 유지
            return "____ " + exampleEn.trim();
        }
        return exampleEn.substring(0, m.start()) + "____" + exampleEn.substring(m.end());
    }

    // 공백 유연: "take off" -> "take\\s+off"
    private String buildFlexiblePhraseRegex(String phrase) {
        String[] parts = phrase.trim().split("\\s+");
        return Arrays.stream(parts)
                .map(Pattern::quote)
                .collect(Collectors.joining("\\s+"));
    }

    // 객관식 보기 구성: 정답 phrase + 오답 phrase 3개
    private List<String> buildMcqPhraseOptions(List<LibraryItem> pool, Long correctItemId, String correctPhrase, int wrongCount) {
        List<String> wrongs = new ArrayList<>();

        if (pool != null && !pool.isEmpty()) {
            List<String> candidates = pool.stream()
                    .filter(it -> it.getId() != null && !it.getId().equals(correctItemId))
                    .map(LibraryItem::getPhrase)
                    .filter(p -> p != null && !p.isBlank())
                    .map(String::trim)
                    .distinct()
                    .collect(Collectors.toList());

            Collections.shuffle(candidates, ThreadLocalRandom.current());

            for (String c : candidates) {
                if (wrongs.size() >= wrongCount) break;
                if (!c.equalsIgnoreCase(correctPhrase.trim())) wrongs.add(c);
            }
        }

        while (wrongs.size() < wrongCount) {
            wrongs.add("dummy option " + (wrongs.size() + 1));
        }

        List<String> options = new ArrayList<>(1 + wrongCount);
        options.add(correctPhrase.trim());
        options.addAll(wrongs);
        Collections.shuffle(options, ThreadLocalRandom.current());
        return options;
    }

    private Set<Long> pickRandomTaskIds(List<ReviewTask> tasks, int count) {
        if (count <= 0) return Set.of();
        List<Long> ids = tasks.stream().map(ReviewTask::getId).collect(Collectors.toList());
        Collections.shuffle(ids, ThreadLocalRandom.current());
        return new HashSet<>(ids.subList(0, Math.min(count, ids.size())));
    }

    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }

    private void applyHint(ReviewQuestion q, String hint) {
        // 1) setHint(String) 있으면 사용
        try {
            Method m = q.getClass().getMethod("setHint", String.class);
            m.invoke(q, hint);
            return;
        } catch (Exception ignored) {}

        // 2) 없으면 hint 필드 직접 세팅(필드명이 다르면 여기만 바꾸면 됨)
        try {
            Field f = q.getClass().getDeclaredField("hint");
            f.setAccessible(true);
            f.set(q, hint);
        } catch (Exception ignored) {}
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
