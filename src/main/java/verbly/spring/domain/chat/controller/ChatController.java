package verbly.spring.domain.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Chat", description = "채팅 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/chatrooms")
public class ChatController {

    private final ChatroomUserService chatroomUserService;
    private final ChatMessageService chatMessageService;
    private final ChatIntegralService chatIntegralService;

    @Operation(
            summary = "채팅방 생성/입장",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = """
                    채팅방을 생성 또는 입장합니다.\n\n
                            - 채팅방이 없으면 생성
                            - 채팅방이 있으면 입장
                            - 새로 입장 시 상대방도 채팅방 멤버로 자동 추가
                            - 스스로 채팅 불가\n\n
                    채팅방 id 반환 - 웹소켓으로 업그레이드 시 채팅방 id를 경로 변수로 사용
            """
    )
    @PostMapping("/{opponentId}")
    public ResponseEntity<ApiResponse<ChatroomEnterResponseDTO>> enterChatroom(
            @Parameter
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(required = true, name = "opponentId", description = "채팅 상대방 id", example = "1")
            @PathVariable Long opponentId) {

        Long participantId = customUserDetails.getUserId();
        ChatroomEnterResponseDTO chatroomEnterResponseDTO = chatroomUserService.enterChatroom(participantId, opponentId);

        return ResponseEntity
                .status(SuccessStatus.CHATROOM_PARTICIPATE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CHATROOM_PARTICIPATE_SUCCESS, chatroomEnterResponseDTO));
    }

    @Operation(
            summary = "채팅방 리스트 조회",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = """
                    참여하고 있는 채팅방을 조회합니다.\n\n
                            - 채팅 이력이 없으면 미조회"""
    )
    @GetMapping()
    public ResponseEntity<ApiResponse<List<OuterChatroomInfoResponseDTO>>> getChatroomList(
            @Parameter
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long participantId = customUserDetails.getUserId();
        List<OuterChatroomInfoResponseDTO> outerChatroomInfoResponseDTOList = chatroomUserService.getChatroomInfoList(participantId);

        return ResponseEntity
                .status(SuccessStatus.CHATROOM_READ_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CHATROOM_READ_SUCCESS, outerChatroomInfoResponseDTOList));
    }

    @Operation(
            summary = "현재 입장한 채팅방 정보 조회",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = """
                    입장한 채팅방의 정보를 조회합니다.\n\n
                            - 채팅방 이름: 상대방 이름
                            - 채팅방 사진: 상대방 프로필 사진
            """
    )
    @GetMapping("/{opponentId}")
    public ResponseEntity<ApiResponse<InnerChatroomInfoResponseDTO>> getChatroomInfo(
            @Parameter
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(required = true, name = "opponentId", description = "채팅 상대방 id", example = "1")
            @PathVariable Long opponentId) {

        Long participantId = customUserDetails.getUserId();
        InnerChatroomInfoResponseDTO innerChatroomInfoResponseDTO = chatroomUserService.getChatroomInfo(participantId, opponentId);

        return ResponseEntity
                .status(SuccessStatus.CHATROOM_READ_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CHATROOM_READ_SUCCESS, innerChatroomInfoResponseDTO));
    }

    // 특정 채팅방의 전체 채팅 조회
    @Operation(
            summary = "현재 입장한 채팅방의 전체 채팅 전체 내역 조회",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = """
                        입장한 채팅방의 채팅 내역 전체를 조회합니다.\n\n
                            - 최신순 정렬
                            - chatroomId: enterChatroom()의 반환값을 사용
                               - enterChatroom(): 입장하려는 채팅방의 id 반환
            """
    )
    @GetMapping("/{chatroomId}/chats")
    public ResponseEntity<ApiResponse<List<ChatMessageResponseDTO>>> getChatMessageList(
            @Parameter
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(required = true, name = "chatroomId", description = "채팅방 id", example = "1")
            @PathVariable Long chatroomId) {

        Long participantId = customUserDetails.getUserId();
        List<ChatMessageResponseDTO> chatMessageResponseDTOList = chatMessageService.getChatMessageList(participantId, chatroomId);

        return ResponseEntity
                .status(SuccessStatus.CHAT_MESSAGE_READ_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CHAT_MESSAGE_READ_SUCCESS, chatMessageResponseDTOList));
    }

    // 채팅방 검색 + 메세지 검색
    @Operation(
            summary = "참여하고 있는 채팅방 또는 상대방의 프로필을 검색어를 통해 조회",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = """
                        참여하고 있는 채팅방 또는 상대방의 프로필을 검색어를 통해 조회합니다.\n\n
                            - case1: 검색어가 포함된 채팅방 제목 존재 => 프로필 조회
                               - 채팅방 프로필 == 상대방 프로필 
                            - case2: 검색어가 포함된 메세지 내역 존재 => 메세지가 포함된 채팅방 조회
                               - 한 채팅방에 검색어가 포함된 채팅 여러 개 => 동일한 채팅방 여러 번 조회
            """
    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<SearchResponseDTO>> getSearchResult(
            @Parameter
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(name = "search", description = "request param: 검색어", example = "안녕")
            @RequestParam String search) {

        Long participantId = customUserDetails.getUserId();
        SearchResponseDTO searchResponseDTO = chatIntegralService.getSearchResult(participantId, search);

        return ResponseEntity
                .status(SuccessStatus.SEARCH_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.SEARCH_SUCCESS, searchResponseDTO));
    }

    // 채팅방 나가기
    @Operation(
            summary = "채팅방 나가기",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = """
                        채팅방에서 퇴장합니다.\n\n
                            - 채팅방 입장과 달리 퇴장은 당사자만 퇴장되고 상대방은 따로 퇴장 필요
                            - 퇴장 시 다시 해당 상대방과 일대일 채팅을 시도해도 이전 채팅 내역은 확인 불가
            """
    )
    @DeleteMapping("/{opponentId}")
    public ResponseEntity<ApiResponse<Void>> quitChatroom(
            @Parameter
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(required = true, name = "opponentId", description = "채팅 상대방 id", example = "1")
            @PathVariable Long opponentId) {

        Long participantId = customUserDetails.getUserId();
        chatroomUserService.quitChatroom(participantId, opponentId);

        return ResponseEntity
                .status(SuccessStatus.CHATROOM_QUIT_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CHATROOM_QUIT_SUCCESS, null));
    }

}
