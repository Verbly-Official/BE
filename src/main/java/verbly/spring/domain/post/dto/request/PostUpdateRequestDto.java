package verbly.spring.domain.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import verbly.spring.domain.post.enums.PostStatus;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostUpdateRequestDto {
    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private PostStatus status;
}
