package verbly.spring.domain.post.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import verbly.spring.domain.post.dto.response.PostResponseDTO;
import verbly.spring.domain.user.entity.User;

import java.util.UUID;

public interface PostQueryService {
    Slice<PostResponseDTO.HomePosts> getHomePosts(Pageable pageable, User viewer);

    Slice<PostResponseDTO.UserPosts> getUserPosts(Pageable pageable, UUID uuid, User viewer);
}
