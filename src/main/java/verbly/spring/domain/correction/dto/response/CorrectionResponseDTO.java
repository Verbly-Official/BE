package verbly.spring.domain.correction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import verbly.spring.domain.correction.enums.CorrectorType;
import verbly.spring.domain.post.enums.PostStatus;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CorrectionResponseDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyCorrectionDto {

        private Long correctionId;
        private Long postId;

        private String title;
        private PostStatus status;

        private CorrectorType correctorType;
        private String correctorName;

        private LocalDateTime correctionCreatedAt;

    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateCorrectionResponseDTO {
        private Long correctionId;
        private Long postId;
    }
}
