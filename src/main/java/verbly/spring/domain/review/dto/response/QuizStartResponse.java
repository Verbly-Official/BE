package verbly.spring.domain.review.dto.response;

import java.time.LocalDateTime;

public record QuizStartResponse(
        Long sessionId,
        int totalQuestions,
        int currentIndex,
        QuizQuestionResponse firstQuestion,
        LocalDateTime createdAt
) {}
