package verbly.spring.domain.review.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import verbly.spring.domain.review.entity.ReviewQuestion;
import verbly.spring.domain.review.entity.ReviewSession;
import verbly.spring.domain.review.entity.ReviewTask;
import verbly.spring.domain.review.enums.ReviewSessionStatus;
import verbly.spring.domain.review.exception.ReviewHandler;
import verbly.spring.domain.review.repository.ReviewQuestionRepository;
import verbly.spring.domain.review.repository.ReviewSessionRepository;
import verbly.spring.domain.review.repository.ReviewTaskRepository;
import verbly.spring.global.common.code.ErrorStatus;

import java.util.Objects;

/**
 * Review(Quiz) 도메인 유효성 검사/권한 확인
 * - LibraryValidator와 동일한 역할
 */
@Component
@RequiredArgsConstructor
public class ReviewValidator {

    private final ReviewSessionRepository reviewSessionRepository;
    private final ReviewTaskRepository reviewTaskRepository;
    private final ReviewQuestionRepository reviewQuestionRepository;

    public ReviewSession validateOwnedSessionForUpdate(Long userId, Long sessionId) {
        return reviewSessionRepository.findByIdAndUserIdForUpdate(sessionId, userId)
                .orElseThrow(() -> new ReviewHandler(ErrorStatus.QUIZ_SESSION_NOT_FOUND));
    }

    public ReviewSession validateOwnedSession(Long userId, Long sessionId) {
        return reviewSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ReviewHandler(ErrorStatus.QUIZ_SESSION_NOT_FOUND));
    }

    public void validateSessionInProgress(ReviewSession session) {
        if (session.getStatus() != ReviewSessionStatus.IN_PROGRESS) {
            throw new ReviewHandler(ErrorStatus.QUIZ_SESSION_NOT_IN_PROGRESS);
        }
    }

    public ReviewQuestion validateQuestionWithTaskAndItem(Long questionId) {
        return reviewQuestionRepository.findWithTaskAndItemById(questionId)
                .orElseThrow(() -> new ReviewHandler(ErrorStatus.QUIZ_QUESTION_NOT_FOUND));
    }

    public void validateQuestionOwnership(Long userId, Long sessionId, ReviewQuestion q) {
        ReviewTask task = q.getReviewTask();
        if (!Objects.equals(task.getUserId(), userId) || !Objects.equals(task.getSessionId(), sessionId)) {
            throw new ReviewHandler(ErrorStatus.QUIZ_FORBIDDEN);
        }
    }

    public ReviewTask validateTaskByOrder(Long sessionId, int taskOrder) {
        return reviewTaskRepository.findBySessionIdAndTaskOrder(sessionId, taskOrder)
                .orElseThrow(() -> new ReviewHandler(ErrorStatus.QUIZ_SESSION_NOT_FOUND));
    }
}
