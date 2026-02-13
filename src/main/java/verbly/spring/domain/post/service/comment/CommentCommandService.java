package verbly.spring.domain.post.service.comment;

import verbly.spring.domain.post.dto.request.CommentRequestDTO;
import verbly.spring.domain.post.dto.response.CommentResponseDTO;

public interface CommentCommandService {
    CommentResponseDTO.getMyComment getMyComment(Long userId, Long postId, CommentRequestDTO.makeComment dto);
}
