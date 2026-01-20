package verbly.spring.domain.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCreateRequestDto {
    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private Long correctorId;
}
