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
    private PostConverter() {}

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

    public static PostResponseDTO.Summary toResponseSummaryDTO(Post post) {
        return PostResponseDTO.Summary.builder()
                .postId(post.getId())
                .authorId(post.getAuthor().getId())
                .authorNickname(post.getAuthor().getNickname())
                .title(post.getTitle())
                .createdAt(post.getCreatedAt())
                .build();
    }

    public static PostResponseDTO.Detail toResponseDetailDTO(Post post) {
        return PostResponseDTO.Detail.builder()
                .postId(post.getId())
                .authorId(post.getAuthor().getId())
                .authorNickname(post.getAuthor().getNickname())
                .title(post.getTitle())
                .content(post.getContent())
                .status(post.getStatus())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    public static PostResponseDTO.hotPost toHotPost(Post post, Boolean isLiked){
        List<String> tags = post.getPostTags().stream()
                .map(postTag -> postTag.getTag().getName())
                .collect(Collectors.toList());
        return PostResponseDTO.hotPost.builder()
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
}
