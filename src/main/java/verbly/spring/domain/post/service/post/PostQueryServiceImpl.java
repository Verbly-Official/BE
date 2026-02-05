package verbly.spring.domain.post.service.post;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.follow.repository.FollowRepository;
import verbly.spring.domain.post.converter.PostConverter;
import verbly.spring.domain.post.dto.response.PostResponseDTO;
import verbly.spring.domain.post.entity.HotPost;
import verbly.spring.domain.post.entity.Post;
import verbly.spring.domain.post.repository.HotPostRepository;
import verbly.spring.domain.post.repository.PostLikeRepository;
import verbly.spring.domain.post.repository.PostRepository;
import verbly.spring.domain.user.entity.User;
import verbly.spring.global.security.auth.CustomUserDetails;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryServiceImpl implements PostQueryService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostConverter postConverter;
    private final HotPostRepository hotPostRepository;
    private final FollowRepository followRepository;

    @Override
    public Slice<PostResponseDTO.HomePosts> getHomePosts(Pageable pageable, User viewer) {
        Slice<Post> postSlice = postRepository.findAll(pageable);

        return postSlice.map(post -> {
            boolean isLiked = false;
            boolean isFollowing = false;
            if (viewer != null) {
                isLiked = postLikeRepository.existsByUserAndPost(viewer, post);
                isFollowing = followRepository.existsFollowByFollowerIdAndFolloweeId(viewer.getId(), post.getAuthor().getId());
            }
            return postConverter.toHomePosts(post, isLiked, isFollowing);
        });
    }

    @Override
    public Slice<PostResponseDTO.UserPosts> getUserPosts(Pageable pageable, UUID uuid, User viewer) {
        Slice<Post> postSlice = postRepository.findAllByAuthor_Uuid(uuid, pageable);
        return postSlice.map(post -> {
            boolean isLiked = false;
            boolean isFollowing = false;
            if (viewer != null) {
                isLiked = postLikeRepository.existsByUserAndPost(viewer, post);
                isFollowing = followRepository.existsFollowByFollowerIdAndFolloweeId(viewer.getId(), post.getAuthor().getId());
            }
            return postConverter.toUserPosts(post, isLiked, isFollowing);
        });
    }

    @Override
    public List<PostResponseDTO.hotPost> getHotPosts(CustomUserDetails userDetails) {
        User viewer = userDetails != null ? userDetails.getUser() : null;

        List<HotPost> hotPosts = hotPostRepository.findAll(Sort.by(Sort.Direction.DESC, "growthScore"));

        return hotPosts.stream()
                .map(hotPost -> {
                    boolean isLiked = false;
                    boolean isFollowing = false;
                    if (viewer != null) {
                        isLiked = postLikeRepository.existsByUserAndPost(viewer, hotPost.getPost());
                        isFollowing = followRepository.existsFollowByFollowerIdAndFolloweeId(viewer.getId(), hotPost.getPost().getAuthor().getId());
                    }
                    return postConverter.toHotPost(hotPost.getPost(), isLiked, isFollowing);
                })
                .toList();
    }
}
