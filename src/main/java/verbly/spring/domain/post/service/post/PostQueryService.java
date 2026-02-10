package verbly.spring.domain.post.service.post;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import verbly.spring.domain.post.dto.response.PostResponseDTO;

import java.util.List;
import java.util.UUID;

public interface PostQueryService {
    Slice<PostResponseDTO.HomePosts> getHomePosts(Pageable pageable, Long viewerId);

    Slice<PostResponseDTO.UserPosts> getUserPosts(Pageable pageable, UUID uuid, Long viewerIdr);

    List<PostResponseDTO.hotPost> getHotPosts(Long viewerId);
}
