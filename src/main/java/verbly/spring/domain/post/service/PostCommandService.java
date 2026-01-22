package verbly.spring.domain.post.service;

import verbly.spring.domain.post.dto.response.PostResponseDTO;

public interface PostCommandService {
    PostResponseDTO.AddPostLike addPostLike(Long postId, Long userId);
}
