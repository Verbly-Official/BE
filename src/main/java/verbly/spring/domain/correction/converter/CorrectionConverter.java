package verbly.spring.domain.correction.converter;

import verbly.spring.domain.correction.dto.response.CorrectionResponseDTO;
import verbly.spring.domain.correction.entity.Correction;
import verbly.spring.domain.correction.enums.CorrectorType;
import verbly.spring.domain.post.entity.Post;
import verbly.spring.domain.post.entity.PostTag;
import verbly.spring.domain.post.entity.Tag;
import verbly.spring.global.common.utils.RelativeTimeUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CorrectionConverter {
    private CorrectionConverter() {}

    public static CorrectionResponseDTO.CreateCorrectionResponseDTO toCreateCorrectionResponse(Correction correction) {
        return CorrectionResponseDTO.CreateCorrectionResponseDTO.builder()
                .correctionId(correction.getId())
                .postId(correction.getPost().getId())
                .build();
    }

    public static CorrectionResponseDTO.MyCorrectionDto toMyCorrectionDTO(
            Correction correction,
            CorrectorType correctorType,
            String correctorName,
            boolean isBookmarked
    ) {
        Post post = correction.getPost();

        List<String> tags = post.getPostTags().stream()
                .map(pt -> pt.getTag().getName())
                .toList();

        return CorrectionResponseDTO.MyCorrectionDto.builder()
                .correctionId(correction.getId())
                .postId(post.getId())
                .title(post.getTitle())
                .status(post.getStatus())
                .bookmark(isBookmarked)
                .content(post.getContent())
                .tags(tags)
                .correctorType(correctorType)
                .correctorName(correctorName)
                .correctionCreatedAt(correction.getCreatedAt())
                .correctionUpdatedAt(correction.getUpdatedAt())
                .build();
    }

    public static CorrectionResponseDTO.MyCorrectionListDto toMyCorrectionListDTO(
            Correction correction,
            CorrectorType latestCorrectorType,
            String latestCorrectorName,
            boolean isBookmarked
    ) {
        Post post = correction.getPost();

        return CorrectionResponseDTO.MyCorrectionListDto.builder()
                .correctionId(correction.getId())
                .postId(post.getId())
                .title(post.getTitle())
                .status(post.getStatus())
                .bookmark(isBookmarked)
                .correctorType(latestCorrectorType)
                .correctorName(latestCorrectorName)
                .correctionCreatedAt(correction.getCreatedAt())
                .correctionUpdatedAt(correction.getUpdatedAt())
                .relativeTime(RelativeTimeUtils.toRelative(correction.getCreatedAt()))
                .build();
    }
}
