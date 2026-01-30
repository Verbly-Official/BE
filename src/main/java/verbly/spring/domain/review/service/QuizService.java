package verbly.spring.domain.review.service;

import verbly.spring.domain.review.dto.request.ReviewResquestDto;
import verbly.spring.domain.review.dto.response.*;

public interface QuizService {

    ReviewResponseDto.QuizStartResponse startSession(Long userId);

    ReviewResponseDto.QuizHintResponse useHint(Long userId, Long sessionId, Long questionId);

    ReviewResponseDto.QuizAnswerSubmitResponse submitAnswer(Long userId, Long sessionId, Long questionId, ReviewResquestDto.QuizAnswerSubmitRequest request);

    ReviewResponseDto.QuizQuitResponse quit(Long userId, Long sessionId);

    ReviewResponseDto.QuizResultResponse getResult(Long userId, Long sessionId);

    ReviewResponseDto.QuizStartResponse retryMistakes(Long userId, Long sessionId);
}
