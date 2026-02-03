package verbly.spring.domain.quizreview.dto.response;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;

public class ReviewResponseDTO {
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

    public record QuizHintResponse(
            Long questionId,
            int hintUsed,
            int hintTotal,
            int hintRemaining,
            String hint
    ) {}

    public record QuizMistakeResponse(
            Long questionId,
            Long libraryItemId,
            String phrase,
            String prompt,
            JsonNode userAnswerJson,
            JsonNode correctAnswerKeyJson,
            String explanation
    ) {}

    public record QuizQuestionResponse(
            Long questionId,
            Long libraryItemId,
            String phrase,
            String questionType,
            String prompt,
            List<String> options

    ) {}

    public  record QuizQuitResponse(
            Long sessionId,
            String status
    ) {}

    public record QuizResultResponse(
            Long sessionId,
            int totalQuestions,
            int correctCount,
            int wrongCount,
            int accuracyPercent,
            List<QuizMistakeResponse> mistakes
    ) {}

    public record QuizStartResponse(
            Long sessionId,
            int totalQuestions,
            int currentIndex,
            QuizQuestionResponse firstQuestion,
            LocalDateTime createdAt
    ) {}
}
