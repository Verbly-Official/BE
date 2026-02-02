package verbly.spring.domain.chat.dto.responseDTO;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import verbly.spring.domain.chat.entity.Chatroom;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ChatroomEnterResponseDTO {

    private Long chatroomId;

    public static ChatroomEnterResponseDTO from(Chatroom chatroom) {

        return ChatroomEnterResponseDTO.builder()
            .chatroomId(chatroom.getId())
            .build();
    }
}
