package verbly.spring.domain.chat.controller;

import java.lang.Void;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import verbly.spring.domain.chat.dto.responseDTO.*;
import verbly.spring.domain.chat.service.ChatIntegralService;
import verbly.spring.domain.chat.service.ChatMessageService;
import verbly.spring.domain.chat.service.ChatroomUserService;
import verbly.spring.global.common.code.SuccessStatus;
import verbly.spring.global.common.response.ApiResponse;
import verbly.spring.global.security.auth.CustomUserDetails;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/chatrooms")
public class ChatController {

    private final ChatroomUserService chatroomUserService;
    private final ChatMessageService chatMessageService;
    private final ChatIntegralService chatIntegralService;

    // 채팅방 참여
    @PostMapping("/{opponentId}")
    public ResponseEntity<ApiResponse<ChatroomEnterResponseDTO>> enterChatroom(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long opponentId) {

        Long participantId = customUserDetails.getUserId();
        ChatroomEnterResponseDTO chatroomEnterResponseDTO = chatroomUserService.enterChatroom(participantId, opponentId);

        return ResponseEntity
                .status(SuccessStatus.CHATROOM_PARTICIPATE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CHATROOM_PARTICIPATE_SUCCESS, chatroomEnterResponseDTO));
    }

    // 사용자 참여 전체 채팅방 조회
    @GetMapping()
    public ResponseEntity<ApiResponse<List<OuterChatroomInfoResponseDTO>>> getChatroomList(@AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long participantId = customUserDetails.getUserId();
        List<OuterChatroomInfoResponseDTO> outerChatroomInfoResponseDTOList = chatroomUserService.getChatroomInfoList(participantId);

        return ResponseEntity
                .status(SuccessStatus.CHATROOM_READ_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CHATROOM_READ_SUCCESS, outerChatroomInfoResponseDTOList));
    }

    // 채팅방 입장 후 상대방 정보 조회-채팅방 정보
    @GetMapping("/{opponentId}")
    public ResponseEntity<ApiResponse<InnerChatroomInfoResponseDTO>> getChatroomInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long opponentId) {

        Long participantId = customUserDetails.getUserId();
        InnerChatroomInfoResponseDTO innerChatroomInfoResponseDTO = chatroomUserService.getChatroomInfo(participantId, opponentId);

        return ResponseEntity
                .status(SuccessStatus.CHATROOM_READ_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CHATROOM_READ_SUCCESS, innerChatroomInfoResponseDTO));
    }

    // 특정 채팅방의 전체 채팅 조회
    @GetMapping("/{chatroomId}/chats")
    public ResponseEntity<ApiResponse<List<ChatMessageResponseDTO>>> getChatMessageList(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long chatroomId) {

        Long participantId = customUserDetails.getUserId();
        List<ChatMessageResponseDTO> chatMessageResponseDTOList = chatMessageService.getChatMessageList(participantId, chatroomId);

        return ResponseEntity
                .status(SuccessStatus.CHAT_MESSAGE_READ_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CHAT_MESSAGE_READ_SUCCESS, chatMessageResponseDTOList));
    }

    // 채팅방 검색 + 메세지 검색
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<SearchResponseDTO>> getSearchResult(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestParam String search) {

        Long participantId = customUserDetails.getUserId();
        SearchResponseDTO searchResponseDTO = chatIntegralService.getSearchResult(participantId, search);

        return ResponseEntity
                .status(SuccessStatus.SEARCH_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.SEARCH_SUCCESS, searchResponseDTO));
    }

    // 채팅방 나가기
    @DeleteMapping("/{opponentId}")
    public ResponseEntity<ApiResponse<Void>> quitChatroom(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long opponentId) {

        Long participantId = customUserDetails.getUserId();
        chatroomUserService.quitChatroom(participantId, opponentId);

        return ResponseEntity
                .status(SuccessStatus.CHATROOM_QUIT_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CHATROOM_QUIT_SUCCESS, null));
    }

}
