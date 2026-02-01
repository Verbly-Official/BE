package verbly.spring.domain.quizreview.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.quizreview.converter.ReviewConverter;
import verbly.spring.domain.quizreview.dto.response.ReviewResponseDTO;
import verbly.spring.domain.quizreview.entity.ReviewAnswer;
import verbly.spring.domain.quizreview.entity.ReviewQuestion;
import verbly.spring.domain.quizreview.entity.ReviewSession;
import verbly.spring.domain.quizreview.entity.ReviewTask;
import verbly.spring.domain.quizreview.enums.ReviewSessionStatus;
import verbly.spring.domain.quizreview.exception.ReviewHandler;
import verbly.spring.domain.quizreview.repository.ReviewAnswerRepository;
import verbly.spring.domain.quizreview.repository.ReviewQuestionRepository;
import verbly.spring.domain.quizreview.repository.ReviewSessionRepository;
import verbly.spring.domain.quizreview.repository.ReviewTaskRepository;
import verbly.spring.global.common.code.ErrorStatus;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewQueryServiceImpl implements ReviewQueryService {

    private final ReviewSessionRepository reviewSessionRepository;
    private final ReviewTaskRepository reviewTaskRepository;
    private final ReviewQuestionRepository reviewQuestionRepository;
    private final ReviewAnswerRepository reviewAnswerRepository;

    @Override
    public ReviewResponseDTO.QuizResultResponse getResult(Long userId, Long sessionId) {
        ReviewSession session = reviewSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ReviewHandler(ErrorStatus.QUIZ_SESSION_NOT_FOUND));

        // 화면/정책상 "완료/중단" 세션만 결과를 조회하게 제한
        if (session.getStatus() == ReviewSessionStatus.IN_PROGRESS) {
            throw new ReviewHandler(ErrorStatus.QUIZ_SESSION_NOT_IN_PROGRESS);
        }

        List<ReviewTask> tasks = reviewTaskRepository.findAllBySessionIdOrderByTaskOrderAsc(sessionId);
        List<Long> taskIds = tasks.stream().map(ReviewTask::getId).toList();

        List<ReviewQuestion> questions = reviewQuestionRepository.findAllByReviewTask_IdIn(taskIds);
        if (questions.isEmpty()) {
            // quit 후 문제를 삭제해버린 케이스면 결과가 없을 수 있음
            return new ReviewResponseDTO.QuizResultResponse(sessionId, 0, 0, 0, 0, List.of());
        }

        List<Long> questionIds = questions.stream().map(ReviewQuestion::getId).toList();

        // 각 question의 마지막 답만 뽑기 (전체 answers 한번에 가져와 메모리에서 그룹)
        List<ReviewAnswer> answers = reviewAnswerRepository
                .findAllByReviewQuestion_IdInOrderByReviewQuestion_IdAscAttemptNoDesc(questionIds);

        Map<Long, ReviewAnswer> lastAnswerByQuestion = new LinkedHashMap<>();
        for (ReviewAnswer a : answers) {
            Long qid = a.getReviewQuestion().getId();
            lastAnswerByQuestion.putIfAbsent(qid, a);
        }

        int total = questions.size();
        int correctCount = 0;
        List<ReviewResponseDTO.QuizMistakeResponse> mistakes = new ArrayList<>();

        for (ReviewQuestion q : questions) {
            ReviewAnswer last = lastAnswerByQuestion.get(q.getId());
            if (last != null && last.isCorrect()) {
                correctCount++;
            } else {
                JsonNode userAnswer = (last == null) ? null : last.getUserAnswerJson();
                mistakes.add(ReviewConverter.toMistakeResponse(q, userAnswer));
            }
        }

        int wrongCount = total - correctCount;
        int accuracy = (total == 0) ? 0 : (int) Math.round((correctCount * 100.0) / total);

        return new ReviewResponseDTO.QuizResultResponse(
                sessionId,
                total,
                correctCount,
                wrongCount,
                accuracy,
                mistakes
        );
    }
}
