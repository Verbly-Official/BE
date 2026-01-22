package verbly.spring.domain.review.service;

import verbly.spring.domain.review.dto.request.QuizAnswerSubmitRequest;
import verbly.spring.domain.review.dto.response.*;

public interface QuizService {

    QuizStartResponse startSession(Long userId);

    QuizHintResponse useHint(Long userId, Long sessionId, Long questionId);

    QuizAnswerSubmitResponse submitAnswer(Long userId, Long sessionId, Long questionId, QuizAnswerSubmitRequest request);

    QuizQuitResponse quit(Long userId, Long sessionId);

    QuizResultResponse getResult(Long userId, Long sessionId);

    QuizStartResponse retryMistakes(Long userId, Long sessionId);
}
