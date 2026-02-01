package verbly.spring.domain.quizreview.service;

import verbly.spring.domain.quizreview.dto.request.ReviewRequestDTO;
import verbly.spring.domain.quizreview.dto.response.ReviewResponseDTO;

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
