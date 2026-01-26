package verbly.spring.domain.chat.controller;

import com.amazonaws.Response;
import java.lang.Void;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import verbly.spring.domain.chat.dto.responseDTO.ChatMessageResponseDTO;
import verbly.spring.domain.chat.dto.responseDTO.ChatroomInfoResponseDTO;
import verbly.spring.domain.chat.dto.responseDTO.SearchResponseDTO;
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
    public ResponseEntity<ApiResponse<Void>> enterChatroom(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long opponentId) {

        Long participantId = customUserDetails.getUserId();
        chatroomUserService.enterChatroom(participantId, opponentId);

        return ResponseEntity
                .status(SuccessStatus.CHATROOM_PARTICIPATE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CHATROOM_PARTICIPATE_SUCCESS, null));
    }

    // 사용자 참여 전체 채팅방 조회
    @GetMapping()
    public ResponseEntity<ApiResponse<List<ChatroomInfoResponseDTO>>> getChatroomList(@AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long participantId = customUserDetails.getUserId();
        List<ChatroomInfoResponseDTO> chatroomInfoResponseDTOList = chatroomUserService.getChatroomInfoList(participantId);

        return ResponseEntity
                .status(SuccessStatus.CHATROOM_READ_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CHATROOM_READ_SUCCESS, chatroomInfoResponseDTOList));
    }

    // 단일 채팅방 조회
    @GetMapping("/{opponentId}")
    public ResponseEntity<ApiResponse<ChatroomInfoResponseDTO>> getChatroomInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long opponentId) {

        Long participantId = customUserDetails.getUserId();
        ChatroomInfoResponseDTO chatroomInfoResponseDTO= chatroomUserService.getChatroomInfo(participantId, opponentId);

        return ResponseEntity
                .status(SuccessStatus.CHAT_MESSAGE_READ_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CHAT_MESSAGE_READ_SUCCESS, chatroomInfoResponseDTO));
    }

    // 특정 채팅방의 전체 채팅 조회
    @GetMapping("/{opponentId}/chats")
    public ResponseEntity<ApiResponse<List<ChatMessageResponseDTO>>> getChatMessageList(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long opponentId) {

        Long participantId = customUserDetails.getUserId();
        List<ChatMessageResponseDTO> chatMessageResponseDTOList = chatMessageService.getChatMessageList(participantId, opponentId);

        return ResponseEntity
                .status(SuccessStatus.CHAT_MESSAGE_READ_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CHAT_MESSAGE_READ_SUCCESS, chatMessageResponseDTOList));
    }

//    // 채팅방 검색 + 메세지 검색
//    @GetMapping()
//    public ResponseEntity<ApiResponse<SearchResponseDTO>> getSearchResult(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestParam String search) {
//
//        Long participantId = customUserDetails.getUserId();
//        SearchResponseDTO searchResponseDTO = chatIntegralService.getSearchResult(participantId, search);
//
//        return ResponseEntity
//                .status(SuccessStatus.SEARCH_SUCCESS.getHttpStatus())
//                .body(ApiResponse.of(SuccessStatus.SEARCH_SUCCESS, searchResponseDTO));
//    }

    // 채팅방 나가기
    @DeleteMapping("/{opponentId}")
    public ResponseEntity<ApiResponse<Void>> quitChatroom(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long opponentId) {

        Long participantId = customUserDetails.getUserId();
        chatroomUserService.quitChatroom(participantId);

        return ResponseEntity
                .status(SuccessStatus.CHATROOM_QUIT_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CHATROOM_QUIT_SUCCESS, null));
    }

}
