package verbly.spring.domain.post.converter;

import verbly.spring.domain.post.dto.response.PostResponseDTO;
import verbly.spring.domain.post.entity.Post;

public class PostConverter {
    private PostConverter() {}

    public static PostResponseDTO.Summary toResponseSummaryDTO(Post post) {
        return PostResponseDTO.Summary.builder()
                .postId(post.getId())
                .authorId(post.getAuthor().getId())
                .authorNickname(post.getAuthor().getNickname())
                .title(post.getTitle())
                .createdAt(post.getCreatedAt())
                .build();
    }

    public static PostResponseDTO.Detail toResponseDetailDTO(Post post) {
        return PostResponseDTO.Detail.builder()
                .postId(post.getId())
                .authorId(post.getAuthor().getId())
                .authorNickname(post.getAuthor().getNickname())
                .title(post.getTitle())
                .content(post.getContent())
                .status(post.getStatus())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
