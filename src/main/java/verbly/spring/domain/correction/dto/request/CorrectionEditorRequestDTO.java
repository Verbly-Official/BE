package verbly.spring.domain.correction.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

public class CorrectionEditorRequestDTO {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpsertWords {
        private List<WordEdit> edits;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WordEdit {
        @NotNull(message = "wordId는 필수입니다.")
        private Long wordId;

        private String correctedText;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WriteFeedback {
        private Integer sentenceIdx;

        @NotBlank
        private String content;
    }

    @Getter
    public static class UpdateFeedback {
        @NotBlank(message = "내용은 필수입니다.")
        private String content;
    }

}
