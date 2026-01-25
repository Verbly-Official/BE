package verbly.spring.domain.chat.dto.responseDTO;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import verbly.spring.domain.chat.entity.ChatroomUser;
import verbly.spring.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ChatroomInfoResponseDTO {

    private String chatroomName;

    private String imageUrl;

    private LocalDateTime lastReadAt;

    private Integer unreadChatCount;

    public static ChatroomInfoResponseDTO from(ChatroomUser chatroomUser, User opponent) {

        return ChatroomInfoResponseDTO.builder()
                .chatroomName(opponent.getNickname())
                .imageUrl(opponent.getProfileImage().getImageUrl())
                .lastReadAt(chatroomUser.getLastReadAt())
                .unreadChatCount(chatroomUser.getUnreadChatCount())
                .build();
    }
}
