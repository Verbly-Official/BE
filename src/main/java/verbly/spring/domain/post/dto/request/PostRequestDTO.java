package verbly.spring.domain.post.dto.request;

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
}
