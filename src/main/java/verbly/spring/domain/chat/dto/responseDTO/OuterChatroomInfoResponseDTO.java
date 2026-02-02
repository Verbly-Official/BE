package verbly.spring.domain.chat.dto.responseDTO;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import verbly.spring.domain.chat.entity.ChatMessage;
import verbly.spring.domain.chat.entity.ChatroomUser;
import verbly.spring.domain.user.entity.User;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class OuterChatroomInfoResponseDTO {

    private Long chatroomId;

    private String chatroomName;

    private String imageUrl;

    private String chatMessage;

    private LocalDateTime chatMessageCreatedAt;

    private Integer unreadChatCount;

    public static OuterChatroomInfoResponseDTO from(ChatroomUser chatroomUser, User opponent, ChatMessage chatMessage, Integer unreadChatCount) {

        return OuterChatroomInfoResponseDTO.builder()
                .chatroomId(chatroomUser.getChatroom().getId())
                .chatroomName(getOpponentNickname(opponent))
                .imageUrl(getOpponentImage(opponent))
                .chatMessage(chatMessage.getChatContent())
                .chatMessageCreatedAt(chatMessage.getCreatedAt())
                .unreadChatCount(unreadChatCount)
                .build();
    }

    private static String getOpponentImage(User opponent) {

        if (opponent == null)
            return null;
        return opponent.getProfileImage().getImageUrl();
    }

    private static String getOpponentNickname(User opponent) {

        if (opponent == null)
            return "존재하지 않는 사용자";
        return opponent.getNickname();
    }
}
