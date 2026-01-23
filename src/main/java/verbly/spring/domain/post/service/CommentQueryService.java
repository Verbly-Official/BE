package verbly.spring.domain.post.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import verbly.spring.domain.post.dto.response.CommentResponseDTO;

public interface CommentQueryService {
    Slice<CommentResponseDTO.getComment> getComments(Pageable pageable, Long postId);
}
