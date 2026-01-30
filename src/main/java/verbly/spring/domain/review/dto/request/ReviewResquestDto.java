package verbly.spring.domain.review.dto.request;

import com.fasterxml.jackson.databind.JsonNode;

public class ReviewResquestDto {

    public record QuizAnswerSubmitRequest(
            JsonNode userAnswerJson,
            String mistakeNote
    ) {}

    public record QuizHintRequest() {}

    public record QuizStartRequest() {}
}
