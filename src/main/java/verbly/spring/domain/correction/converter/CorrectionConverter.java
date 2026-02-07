package verbly.spring.domain.correction.converter;

import verbly.spring.domain.correction.dto.response.CorrectionResponseDTO;
import verbly.spring.domain.correction.entity.Correction;
import verbly.spring.domain.correction.enums.CorrectorType;
import verbly.spring.domain.post.entity.Post;
import verbly.spring.domain.post.entity.PostTag;
import verbly.spring.domain.post.entity.Tag;

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
            CorrectorType latestCorrectorType,
            String latestCorrectorName
    ) {
        Post post = correction.getPost();

        List<String> tags = (post.getPostTags() == null)
                ? Collections.emptyList()
                : post.getPostTags().stream()
                .map(PostTag::getTag)
                .filter(Objects::nonNull)
                .map(Tag::getName)
                .filter(Objects::nonNull)
                .toList();


        return CorrectionResponseDTO.MyCorrectionDto.builder()
                .correctionId(correction.getId())
                .postId(post.getId())
                .title(post.getTitle())
                .tags(tags)
                .correctorType(latestCorrectorType)
                .correctorName(latestCorrectorName)
                .correctionCreatedAt(correction.getCreatedAt())
                .correctionUpdatedAt(correction.getUpdatedAt())
                .status(post.getStatus())
                .build();
    }
}
