package verbly.spring.domain.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class PostRequestDTO {
    @Getter
    @NoArgsConstructor
    public static class HomeWritePost {
        String content;
        Boolean publicSetting;
        List<String> tags;
    }

    @Getter
    @NoArgsConstructor
    public static class tempDto {
        @NotBlank(message = "글 제목은 필수입니다.")
        private String title;

        @NotBlank(message = "글 내용은 필수입니다.")
        private String content;
    }
}
