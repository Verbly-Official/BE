package verbly.spring.domain.quizreview.service;

import verbly.spring.domain.quizreview.dto.response.ReviewResponseDTO;

public interface ReviewQueryService {

    ReviewResponseDTO.QuizResultResponse getResult(Long userId, Long sessionId);
}
