package verbly.spring.domain.chat.dto.responseDTO;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import verbly.spring.domain.chat.entity.ChatroomUser;

import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class SearchResponseDTO {

    // 프로필-채팅방 이름과 사진
    private List<ChatProfileResponseDTO> chatProfileResponseDTOList;

    // 채팅방
    private List<OuterChatroomInfoResponseDTO> outerChatroomInfoResponseDTOList;

    public static SearchResponseDTO from(List<ChatProfileResponseDTO> chatProfileResponseDTOList, List<OuterChatroomInfoResponseDTO> outerChatroomInfoResponseDTOList) {

        return SearchResponseDTO.builder()
                .chatProfileResponseDTOList(chatProfileResponseDTOList)
                .outerChatroomInfoResponseDTOList(outerChatroomInfoResponseDTOList)
                .build();
    }
}
