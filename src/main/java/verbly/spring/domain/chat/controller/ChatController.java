package verbly.spring.domain.chat.controller;

import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import verbly.spring.global.common.response.ApiResponse;
import verbly.spring.global.security.auth.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/chatrooms")
public class ChatController {

    // 채팅방 참여
    @PostMapping("/{chatroomId}")
    public ResponseEntity<ApiResponse<Null>> enterChatroom(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long roomId) {

        Long participantId = customUserDetails.getUserId();
    }

    // 사용자의 전체 채팅방 조회


    // 전체 채팅 조회


    // 특정 채팅방 검색


    // 특정 메세지 검색


    // 채팅방 나가기


}
