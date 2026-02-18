package verbly.spring.global.infrastructure.openAI.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OpenAIMessage {
    private String role;    // system, user, assistant
    private String content; // message content

    public OpenAIMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }
}
