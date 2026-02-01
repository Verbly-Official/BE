package verbly.spring.domain.quizreview.converter;

import com.fasterxml.jackson.databind.JsonNode;
import verbly.spring.domain.quizreview.dto.response.ReviewResponseDTO;
import verbly.spring.domain.quizreview.entity.ReviewQuestion;

import java.util.ArrayList;
import java.util.List;

/**
 * Review(Quiz) 도메인 Entity -> DTO 변환 전용 클래스
 * - LibraryConverter와 동일한 역할
 */
public class ReviewConverter {

    private ReviewConverter() {}

    /**
     * ReviewQuestion -> QuizQuestionResponse
     */
    public static ReviewResponseDTO.QuizQuestionResponse toQuestionResponse(ReviewQuestion q) {
        List<String> options = new ArrayList<>();
        if (q.getOptionsJson() != null && q.getOptionsJson().isArray()) {
            for (JsonNode node : q.getOptionsJson()) {
                options.add(node.asText());
            }
        }

        return new ReviewResponseDTO.QuizQuestionResponse(
                q.getId(),
                q.getLibraryItem().getId(),
                q.getLibraryItem().getPhrase(),
                q.getQuestionType().name(),
                q.getPrompt(),
                options,
                q.getHintTotal(),
                q.getHintUsed()
        );
    }

    public static ReviewResponseDTO.QuizMistakeResponse toMistakeResponse(
            ReviewQuestion q,
            JsonNode userAnswerJson
    ) {
        return new ReviewResponseDTO.QuizMistakeResponse(
                q.getId(),
                q.getLibraryItem().getId(),
                q.getLibraryItem().getPhrase(),
                q.getPrompt(),
                userAnswerJson,
                q.getAnswerKeyJson(),
                q.getExplanation()
        );
    }
}
