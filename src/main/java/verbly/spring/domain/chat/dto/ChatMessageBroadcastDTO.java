package verbly.spring.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageBroadcastDTO {

    private Long messageId;

    private Long chatroomId;

    private Long senderId;

    private String chatContent;

    private LocalDateTime createdAt;
}
