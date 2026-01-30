package verbly.spring.domain.review.service;

import verbly.spring.domain.review.dto.request.ReviewRequestDTO;
import verbly.spring.domain.review.dto.response.ReviewResponseDTO;

public interface ReviewCommandService {

    ReviewResponseDTO.QuizStartResponse startSession(Long userId);

    ReviewResponseDTO.QuizHintResponse useHint(Long userId, Long sessionId, Long questionId);

    ReviewResponseDTO.QuizAnswerSubmitResponse submitAnswer(
            Long userId,
            Long sessionId,
            Long questionId,
            ReviewRequestDTO.QuizAnswerSubmitRequest request
    );

    ReviewResponseDTO.QuizQuitResponse quit(Long userId, Long sessionId);

    ReviewResponseDTO.QuizStartResponse retryMistakes(Long userId, Long sessionId);
}
