package verbly.spring.domain.chat.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ChatMessageRequestDTO {

    @NotBlank
    private String content;
}
