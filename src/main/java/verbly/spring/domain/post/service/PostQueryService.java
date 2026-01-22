package verbly.spring.domain.post.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import verbly.spring.domain.post.dto.response.PostResponseDTO;

public interface PostQueryService {
    Slice<PostResponseDTO.HomePosts> getHomePosts(Pageable pageable);
}
