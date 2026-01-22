package verbly.spring.domain.review.dto.response;

public record QuizHintResponse(
        Long questionId,
        int hintUsed,
        int hintTotal,
        int hintRemaining
) {}
