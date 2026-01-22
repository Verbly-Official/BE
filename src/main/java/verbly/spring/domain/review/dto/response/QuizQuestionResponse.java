package verbly.spring.domain.review.dto.response;

import java.util.List;

public record QuizQuestionResponse(
        Long questionId,
        Long libraryItemId,
        String phrase,
        String questionType,
        String prompt,
        List<String> options,
        int hintTotal,
        int hintUsed
) {}
