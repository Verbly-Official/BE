package verbly.spring.domain.chat.dto.responseDTO;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import verbly.spring.domain.chat.entity.ChatroomUser;
import verbly.spring.domain.user.entity.User;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ChatProfileResponseDTO {

    private Long chatroomId;

    private String chatroomName;

    private String imageUrl;

    public static ChatProfileResponseDTO from(Long chatroomId, User opponent) {

        return ChatProfileResponseDTO.builder()
                .chatroomId(chatroomId)
                .chatroomName(opponent.getNickname())
                .imageUrl(opponent.getProfileImage().getImageUrl())
                .build();
    }
}
