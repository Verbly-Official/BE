package verbly.spring.domain.post.converter;

import org.springframework.stereotype.Component;
import verbly.spring.domain.post.dto.response.PostResponseDTO;
import verbly.spring.domain.post.entity.Post;
import verbly.spring.domain.post.repository.PostLikeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PostConverter {

    public PostResponseDTO.HomePosts toHomePosts(Post post, Boolean isLiked) {
        List<String> tags = post.getPostTags().stream()
                .map(postTag -> postTag.getTag().getName())
                .collect(Collectors.toList());
        return PostResponseDTO.HomePosts.builder()
                .postId(post.getId())
                .content(post.getContent())
                .commentsCount(post.getCommentsCount())
                .isCorrected(post.getIsCorrected())
                .likesCount(post.getLikesCount())
                .createdAt(post.getCreatedAt())
                .tags(tags)
                .userImageUrl(post.getUser().getProfileImage().getImageUrl().toString())
                .uuid(post.getUser().getUuid())
                .nickname(post.getUser().getNickname())
                .isFollowing(false)
                //Follow 구현 후 수정 예정
                .isLiked(isLiked)
                .build();
    }
}
