package verbly.spring.domain.review.dto.response;

import com.fasterxml.jackson.databind.JsonNode;

public record QuizAnswerSubmitResponse(
        Long questionId,
        boolean isCorrect,
        JsonNode correctAnswerKeyJson,
        String explanation,
        int currentIndex,
        int totalQuestions,
        boolean sessionCompleted,
        QuizQuestionResponse nextQuestion
) {}
