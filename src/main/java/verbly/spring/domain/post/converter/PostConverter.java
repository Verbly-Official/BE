package verbly.spring.domain.post.converter;

import org.springframework.stereotype.Component;
import verbly.spring.domain.post.dto.request.PostRequestDTO;
import verbly.spring.domain.post.dto.response.PostResponseDTO;
import verbly.spring.domain.post.entity.Post;
import verbly.spring.domain.post.enums.PostStatus;
import verbly.spring.domain.user.entity.User;

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
                .status(post.getStatus())
                .likesCount(post.getLikesCount())
                .createdAt(post.getCreatedAt())
                .tags(tags)
                .userImageUrl(post.getAuthor().getProfileImage().getImageUrl().toString())
                .uuid(post.getAuthor().getUuid())
                .nickname(post.getAuthor().getNickname())
                .isFollowing(false)
                //Follow 구현 후 수정 예정
                .isLiked(isLiked)
                .build();
    }

    public PostResponseDTO.UserPosts toUserPosts(Post post, Boolean isLiked) {
        List<String> tags = post.getPostTags().stream()
                .map(postTag -> postTag.getTag().getName())
                .collect(Collectors.toList());
        return PostResponseDTO.UserPosts.builder()
                .postId(post.getId())
                .content(post.getContent())
                .commentsCount(post.getCommentsCount())
                .status(post.getStatus())
                .likesCount(post.getLikesCount())
                .createdAt(post.getCreatedAt())
                .tags(tags)
                .userImageUrl(post.getAuthor().getProfileImage().getImageUrl().toString())
                .uuid(post.getAuthor().getUuid())
                .nickname(post.getAuthor().getNickname())
                .isFollowing(false)
                //Follow 구현 후 수정 예정
                .isLiked(isLiked)
                .build();
    }

    public PostResponseDTO.AddPostLike addPostLike(Post post, Boolean isLiked){
        return PostResponseDTO.AddPostLike.builder()
                .isLiked(isLiked)
                .likesCount(post.getLikesCount())
                .postId(post.getId())
                .build();
    }

    public Post toWritePost(PostRequestDTO.HomeWritePost dto, User user) {
        return Post.builder()
                .title("temp")
                .content(dto.getContent())
                .status(PostStatus.PENDING)
                .temp(false)
                .publicSetting(dto.getPublicSetting())
                .author(user)
                .build();
    }

    public PostResponseDTO.HomeWritePost writeHomePost(Post post) {
        return PostResponseDTO.HomeWritePost.builder()
                .postId(post.getId())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
