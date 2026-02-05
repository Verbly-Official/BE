package verbly.spring.domain.post.service.post;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import verbly.spring.domain.post.dto.response.PostResponseDTO;
import verbly.spring.domain.user.entity.User;
import verbly.spring.global.security.auth.CustomUserDetails;

import java.util.List;
import java.util.UUID;

public interface PostQueryService {
    Slice<PostResponseDTO.HomePosts> getHomePosts(Pageable pageable, User viewer);

    Slice<PostResponseDTO.UserPosts> getUserPosts(Pageable pageable, UUID uuid, User viewer);

    List<PostResponseDTO.hotPost> getHotPosts(CustomUserDetails userDetails);
}
