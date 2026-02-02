package verbly.spring.domain.correction.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
        @NotNull(message = "문장 index는 필수입니다.")
        private Integer sentenceIdx;

        @NotNull(message = "문장에서 수정할 블록의 시작 index는 필수입니다.")
        private Integer startIdx;

        @NotNull(message = "문장에서 수정할 블록의 끝 index는 필수입니다.")
        private  Integer endIdx;

        @NotBlank(message = "원문은 필수입니다.")
        private String originalText;

        @NotBlank(message = "교정된 문자열은 필수입니다.")
        private String correctedText;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WriteFeedback {
        private Long correctionWordId;
        private String content;
    }
}
