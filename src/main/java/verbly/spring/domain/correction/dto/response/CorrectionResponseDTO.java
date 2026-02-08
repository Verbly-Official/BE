package verbly.spring.domain.correction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import verbly.spring.domain.correction.enums.CorrectorType;
import verbly.spring.domain.post.enums.PostStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CorrectionResponseDTO {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyCorrectionListDto {

        private Long correctionId;
        private Long postId;

        private String title;
        private PostStatus status;
        private Boolean bookmark;

        private CorrectorType correctorType;
        private String correctorName;

        private String firstTag;
        private Integer wordCount;
        private Long changeCount;

        // UI 반환용 상대 시간
        private String relativeTime;

        private LocalDateTime correctionCreatedAt;
        private LocalDateTime correctionUpdatedAt;

    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyCorrectionDto {

        private Long correctionId;
        private Long postId;

        private String title;
        private PostStatus status;
        private Boolean bookmark;
        private String content;

        private List<String> tags;

        private CorrectorType correctorType;
        private String correctorName;

        private Integer wordCount;

        private LocalDateTime correctionCreatedAt;
        private LocalDateTime correctionUpdatedAt;

    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateCorrectionResponseDTO {
        private Long correctionId;
        private Long postId;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NativeCorrectionDTO {
        private List<MyCorrectionDto> corrections;

        private long totalRequest;
        private int page;
        private int size;
        private int totalPages;
        private boolean hasNext;

        public static NativeCorrectionDTO from(
                Page<MyCorrectionDto> pageResult,
                long totalRequest
        ) {
            return NativeCorrectionDTO.builder()
                    .corrections(pageResult.getContent())
                    .totalRequest(totalRequest)
                    .page(pageResult.getNumber() + 1)
                    .size(pageResult.getSize())
                    .totalPages(pageResult.getTotalPages())
                    .hasNext(pageResult.hasNext())
                    .build();
        }
    }
}
