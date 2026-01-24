package verbly.spring.domain.post.service.comment;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import verbly.spring.domain.post.converter.CommentConverter;
import verbly.spring.domain.post.dto.request.CommentRequestDTO;
import verbly.spring.domain.post.dto.response.CommentResponseDTO;
import verbly.spring.domain.post.entity.Comment;
import verbly.spring.domain.post.entity.Post;
import verbly.spring.domain.post.repository.CommentRepository;
import verbly.spring.domain.post.repository.PostRepository;
import verbly.spring.domain.user.entity.User;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentCommandServiceImpl implements CommentCommandService{

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommentConverter commentConverter;

    @Override
    public CommentResponseDTO.getMyComment getMyComment(User user, Long postId, CommentRequestDTO.makeComment dto) {
        Post post = postRepository.findById(postId).orElse(null);
        Comment comment = commentConverter.toComment(dto, user, post);
        commentRepository.save(comment);
        postRepository.increaseCommentCount(postId);

        Post updatedPost = postRepository.findById(postId).orElse(null);
        return commentConverter.toCommnetDTO(comment, updatedPost);
    }
}
