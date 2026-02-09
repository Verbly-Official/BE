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
        private String userImageUrl;
        private String nickname;
        private Boolean isFollowing;
        private UUID uuid;

        private Long postId;
        private String content;
        private PostStatus status;
        private Integer likesCount;
        private Integer commentsCount;
        private LocalDateTime createdAt;

        private List<String> tags;

        private Boolean isLiked;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddPostLike{
        private Long postId;
        private Integer likesCount;
        private Boolean isLiked;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPosts{
        private String userImageUrl;
        private String nickname;
        private Boolean isFollowing;
        private UUID uuid;

        private Long postId;
        private String content;
        private PostStatus status;
        private Integer likesCount;
        private Integer commentsCount;
        private LocalDateTime createdAt;

        private List<String> tags;

        private Boolean isLiked;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HomeWritePost{
        private Long postId;
        private LocalDateTime createdAt;
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

    @Getter
    @Builder
    public static class hotPost {
        private String userImageUrl;
        private String nickname;
        private Boolean isFollowing;
        private UUID uuid;

        private Long postId;
        private String content;
        private PostStatus status;
        private Integer likesCount;
        private Integer commentsCount;
        private LocalDateTime createdAt;

        private List<String> tags;

        private Boolean isLiked;
    }
}
