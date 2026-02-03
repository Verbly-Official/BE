package verbly.spring.domain.post.service.post;

import verbly.spring.domain.post.dto.request.PostRequestDTO;
import verbly.spring.domain.post.dto.response.PostResponseDTO;
import verbly.spring.domain.user.entity.User;

public interface PostCommandService {
    PostResponseDTO.AddPostLike addPostLike(Long postId, Long userId);

    PostResponseDTO.AddPostLike deletePostLike(Long postId, Long userId);

    PostResponseDTO.HomeWritePost writeHomePost(PostRequestDTO.HomeWritePost dto, User user);
}
