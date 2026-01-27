package verbly.spring.domain.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostRequestDTO {
    @NotBlank(message = "글 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "글 내용은 필수입니다.")
    private String content;
}
