package verbly.spring.domain.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import verbly.spring.domain.post.enums.PostStatus;

import java.time.Instant;

@AllArgsConstructor
@Builder
public class PostListItemResponseDto {
    private Long id;
    private PostStatus status;
    private boolean bookmarked;
    private Instant createdAt;
    private Instant updatedAt;
}
