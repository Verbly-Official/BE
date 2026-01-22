package verbly.spring.domain.review.dto.response;

import com.fasterxml.jackson.databind.JsonNode;

public record QuizMistakeResponse(
        Long questionId,
        Long libraryItemId,
        String phrase,
        String prompt,
        JsonNode userAnswerJson,
        JsonNode correctAnswerKeyJson,
        String explanation
) {}
