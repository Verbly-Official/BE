package verbly.spring.domain.correction.converter;

import verbly.spring.domain.correction.dto.response.CorrectionResponseDTO;
import verbly.spring.domain.post.entity.Post;

public class CorrectionConverter {
    public static CorrectionResponseDTO toResponseDTO(Post post) {
        return CorrectionResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .bookmark(post.isBookmark())
                .isTemp(post.isTemp())
                .status(post.getStatus().name())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
