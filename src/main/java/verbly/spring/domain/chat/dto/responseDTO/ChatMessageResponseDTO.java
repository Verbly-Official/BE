package verbly.spring.domain.chat.dto.responseDTO;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import verbly.spring.domain.chat.entity.ChatMessage;
import verbly.spring.domain.user.entity.User;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ChatMessageResponseDTO {

    private String senderName;

    private String senderImageUrl;

    private String chatContent;

    private LocalDateTime createdAt;

    public static ChatMessageResponseDTO from(ChatMessage chatMessage) {

        User sender = chatMessage.getSender();

        return ChatMessageResponseDTO.builder()
                .senderName(sender.getNickname())
                .senderImageUrl(sender.getProfileImage().getImageUrl())
                .chatContent(chatMessage.getChatContent())
                .createdAt(chatMessage.getCreatedAt())
                .build();
    }
}
