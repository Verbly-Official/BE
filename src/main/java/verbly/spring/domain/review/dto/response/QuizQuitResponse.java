package verbly.spring.domain.review.dto.response;

public record QuizQuitResponse(
        Long sessionId,
        String status
) {}
