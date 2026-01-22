package verbly.spring.domain.post.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import verbly.spring.domain.post.converter.PostConverter;
import verbly.spring.domain.post.dto.response.PostResponseDTO;
import verbly.spring.domain.post.entity.Post;
import verbly.spring.domain.post.entity.PostLike;
import verbly.spring.domain.post.repository.PostLikeRepository;
import verbly.spring.domain.post.repository.PostRepository;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class PostCommandServiceImpl implements PostCommandService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostConverter postConverter;

    @Override
    public PostResponseDTO.AddPostLike addPostLike(Long postId, Long userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("유저가 없습니다."));
        if (postLikeRepository.existsByUserAndPost(user, post)) {
            throw new IllegalStateException("이미 좋아요를 눌렀습니다.");
        }
        postLikeRepository.save(new PostLike(user, post));
        postRepository.increaseLikeCount(postId);
        Post updatedPost = postRepository.findById(postId).orElseThrow();
        Boolean isLiked = postLikeRepository.existsByUserAndPost(user, updatedPost);
        return postConverter.addPostLike(updatedPost, isLiked);
    }
}
