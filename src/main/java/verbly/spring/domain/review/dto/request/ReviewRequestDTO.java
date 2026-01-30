package verbly.spring.domain.review.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReviewRequestDTO {

    public record QuizAnswerSubmitRequest(
            @NotNull JsonNode userAnswerJson,
            @Size(max = 1000) String mistakeNote
    ) {}

    public record QuizHintRequest() {}

    public record QuizStartRequest() {}
}
