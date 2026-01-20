package verbly.spring.domain.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import verbly.spring.domain.post.enums.PostStatus;

import java.time.Instant;

@AllArgsConstructor
@Builder
public class PostResponseDto {
    private Long id;
    private Long authorId;
    private Long correctorId;
    private PostStatus status;
    private String content;
    private boolean bookmarked;
    private Instant createdAt;
    private Instant updatedAt;
}
