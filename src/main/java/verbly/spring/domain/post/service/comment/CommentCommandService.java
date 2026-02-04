package verbly.spring.domain.post.service.comment;

import verbly.spring.domain.post.dto.request.CommentRequestDTO;
import verbly.spring.domain.post.dto.response.CommentResponseDTO;
import verbly.spring.domain.user.entity.User;

public interface CommentCommandService {
    CommentResponseDTO.getMyComment getMyComment(User user, Long postId, CommentRequestDTO.makeComment dto);
}
