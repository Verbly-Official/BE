package verbly.spring.domain.post.service.post;

import verbly.spring.domain.post.dto.response.PostResponseDTO;

public interface PostCommandService {
    PostResponseDTO.AddPostLike addPostLike(Long postId, Long userId);

    PostResponseDTO.AddPostLike deletePostLike(Long postId, Long userId);
}
