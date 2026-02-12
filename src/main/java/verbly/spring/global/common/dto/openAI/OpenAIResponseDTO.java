package verbly.spring.global.common.dto.openAI;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OpenAIResponseDTO {
    private List<Choice> choices;

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Choice {
        private OpenAIMessage message;
    }
}
