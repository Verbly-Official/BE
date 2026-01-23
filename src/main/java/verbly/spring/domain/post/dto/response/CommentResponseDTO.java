package verbly.spring.domain.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;


public class CommentResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class getComment{
        UUID uuid;
        String nickname;
        String userImageUrl;

        String content;
        LocalDateTime createdAt;
    }
}
