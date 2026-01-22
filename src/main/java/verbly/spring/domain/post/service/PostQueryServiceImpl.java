package verbly.spring.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.post.converter.PostConverter;
import verbly.spring.domain.post.dto.response.PostResponseDTO;
import verbly.spring.domain.post.entity.Post;
import verbly.spring.domain.post.repository.PostLikeRepository;
import verbly.spring.domain.post.repository.PostRepository;
import verbly.spring.domain.user.entity.User;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryServiceImpl implements PostQueryService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostConverter postConverter;
    @Override
    public Slice<PostResponseDTO.HomePosts> getHomePosts(Pageable pageable, User viewer) {
        Slice<Post> postSlice = postRepository.findAll(pageable);

        return postSlice.map(post -> {
            boolean isLiked = false;
            if (viewer != null) {
                isLiked = postLikeRepository.existsByUserAndPost(viewer, post);
            }
            return postConverter.toHomePosts(post, isLiked);
        });
    }
}
