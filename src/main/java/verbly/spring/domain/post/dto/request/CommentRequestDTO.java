package verbly.spring.domain.post.dto.request;

import lombok.Getter;

public class CommentRequestDTO {

    @Getter
    public static class makeComment {
        String content;
    }
}
