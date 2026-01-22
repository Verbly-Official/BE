package verbly.spring.domain.review.dto.request;

import com.fasterxml.jackson.databind.JsonNode;

public record QuizAnswerSubmitRequest(
        JsonNode userAnswerJson,
        String mistakeNote
) {}
