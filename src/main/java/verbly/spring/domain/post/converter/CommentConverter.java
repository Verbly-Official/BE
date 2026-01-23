package verbly.spring.domain.post.converter;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import verbly.spring.domain.post.dto.response.CommentResponseDTO;
import verbly.spring.domain.post.entity.Comment;

@Component
public class CommentConverter {
    public CommentResponseDTO.getComment toGetCommnet (Comment comment){
        return CommentResponseDTO.getComment.builder()
                .userImageUrl(comment.getUser().getProfileImage().getImageUrl())
                .nickname(comment.getUser().getNickname())
                .uuid(comment.getUser().getUuid())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
