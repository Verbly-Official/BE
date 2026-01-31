package verbly.spring.domain.correction.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CorrectionRequestDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDTO{
        private Long tempPostId;

        @NotBlank(message = "글 제목은 필수입니다.")
        private String title;

        @NotBlank(message = "글 내용은 필수입니다.")
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDTO{
        private String title;
        private String content;
    }
}
