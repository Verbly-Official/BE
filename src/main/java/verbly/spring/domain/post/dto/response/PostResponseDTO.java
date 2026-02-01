package verbly.spring.domain.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import verbly.spring.domain.post.enums.PostStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PostResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HomePosts{
        String userImageUrl;
        String nickname;
        Boolean isFollowing;
        UUID uuid;

        Long postId;
        String content;
        PostStatus status;
        Integer likesCount;
        Integer commentsCount;
        LocalDateTime createdAt;

        List<String> tags;

        Boolean isLiked;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddPostLike{
        Long postId;
        Integer likesCount;
        Boolean isLiked;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPosts{
        String userImageUrl;
        String nickname;
        Boolean isFollowing;
        UUID uuid;

        Long postId;
        String content;
        PostStatus status;
        Integer likesCount;
        Integer commentsCount;
        LocalDateTime createdAt;

        List<String> tags;

        Boolean isLiked;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HomeWritePost{
        Long postId;
        LocalDateTime createdAt;
    }

    @Getter
    @Builder
    public static class Detail {
        private Long postId;
        private Long authorId;
        private String authorNickname;
        private String title;
        private String content;
        private PostStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter
    @Builder
    public static class Summary {
        private Long postId;
        private Long authorId;
        private String authorNickname;
        private String title;
        private LocalDateTime createdAt;
    }
}
