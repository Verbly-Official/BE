package verbly.spring.domain.chat.dto.responseDTO;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import verbly.spring.domain.chat.entity.ChatroomUser;
import verbly.spring.domain.user.entity.User;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class InnerChatroomInfoResponseDTO {

    private String chatroomName;

    private String imageUrl;

    private LocalDateTime lastReadAt;

    private String nativeLang;

    public static InnerChatroomInfoResponseDTO from(ChatroomUser chatroomUser, User opponent) {

        return InnerChatroomInfoResponseDTO.builder()
                .chatroomName(opponent.getNickname())
                .imageUrl(opponent.getProfileImage().getImageUrl())
                .lastReadAt(chatroomUser.getLastReadAt())
                .nativeLang(opponent.getNativeLang())
                .build();
    }
}
