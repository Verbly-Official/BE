package verbly.spring.global.common.dto.openAI;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OpenAIRequestDTO {
    private String model;
    private List<OpenAIMessage> messages;
    private Double temperature;

    private Map<String, Object> response_format;

    public OpenAIRequestDTO(
            String model,
            List<OpenAIMessage> messages,
            Double temperature,
            Map<String, Object> response_format
    ) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
        this.response_format = response_format;
    }
}
