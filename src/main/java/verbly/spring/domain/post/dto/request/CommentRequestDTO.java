package verbly.spring.domain.post.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

public class CommentRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class makeComment {
        String content;
    }
}
