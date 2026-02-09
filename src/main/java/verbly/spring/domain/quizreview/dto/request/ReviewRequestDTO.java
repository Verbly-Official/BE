package verbly.spring.domain.quizreview.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

public class ReviewRequestDTO {

    public record QuizAnswerSubmitRequest(
            @NotNull JsonNode userAnswerJson
    ) {}

    public record QuizHintRequest() {}

    public record QuizStartRequest() {}
}
