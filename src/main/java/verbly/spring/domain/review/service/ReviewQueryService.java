package verbly.spring.domain.review.service;

import verbly.spring.domain.review.dto.response.ReviewResponseDTO;

public interface ReviewQueryService {

    ReviewResponseDTO.QuizResultResponse getResult(Long userId, Long sessionId);
}
