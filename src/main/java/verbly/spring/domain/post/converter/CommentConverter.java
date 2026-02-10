package verbly.spring.domain.post.converter;

import org.springframework.stereotype.Component;
import verbly.spring.domain.post.dto.request.CommentRequestDTO;
import verbly.spring.domain.post.dto.response.CommentResponseDTO;
import verbly.spring.domain.post.entity.Comment;
import verbly.spring.domain.post.entity.Post;
import verbly.spring.domain.user.entity.User;

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

    public Comment toComment (CommentRequestDTO.makeComment dto, User user, Post post){
        return Comment.builder()
                .content(dto.getContent())
                .user(user)
                .post(post)
                .build();
    }

    public CommentResponseDTO.getMyComment toCommnetDTO (Comment comment, Post post){
        return CommentResponseDTO.getMyComment.builder()
                .userImageUrl(comment.getUser().getProfileImage().getImageUrl())
                .nickname(comment.getUser().getNickname())
                .uuid(comment.getUser().getUuid())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .totalComments(post.getCommentsCount())
                .build();
    }
}
