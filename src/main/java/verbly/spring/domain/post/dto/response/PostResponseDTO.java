package verbly.spring.domain.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import verbly.spring.domain.post.enums.PostStatus;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class PostResponseDTO {

    @Getter
    @Builder
    public static class Summary {
        private Long postId;
        private Long authorId;
        private String authorNickname;
        private String content;
        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    public static class Detail {
        private Long postId;
        private Long authorId;
        private String authorNickname;
        private String content;
        private PostStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
