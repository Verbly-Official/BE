package verbly.spring.domain.correction.dto.response;

import lombok.Builder;
import lombok.Getter;
import verbly.spring.domain.correction.enums.CorrectorType;

import java.time.LocalDateTime;
import java.util.List;

public class CorrectionEditorResponseDTO {
    @Getter
    @Builder
    public static class Detail {
        private final Long correctionId;
        private final Long postId;
        private final List<Sentence> sentences;
        private final List<Word> words;
        private final List<Feedback> feedback;
    }

    @Getter
    @Builder
    public static class Sentence {
        private final Integer idx;
        private final String originalText;
        private String correctedText;
    }

    @Getter
    @Builder
    public static class Word {
        private final Long wordId;
        private final Integer sentenceIdx;
        private final Integer startIdx;
        private final Integer endIdx;
        private final String originalText;
        private final String correctedText;
    }

    @Getter
    @Builder
    public static class Feedback {
        private final Long feedbackId;
        private final Integer sentenceIdx;
        private final String correctorName;
        private final CorrectorType correctorType;
        private final String content;
        private final LocalDateTime createdAt;
        private final LocalDateTime updatedAt;
    }

    @Getter
    @Builder
    public static class WriteFeedbackResult {
        private final Long feedbackId;
    }
}
