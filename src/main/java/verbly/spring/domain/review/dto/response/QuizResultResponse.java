package verbly.spring.domain.review.dto.response;

import java.util.List;

public record QuizResultResponse(
        Long sessionId,
        int totalQuestions,
        int correctCount,
        int wrongCount,
        int accuracyPercent,
        List<QuizMistakeResponse> mistakes
) {}
