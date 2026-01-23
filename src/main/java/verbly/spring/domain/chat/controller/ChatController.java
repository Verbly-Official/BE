package verbly.spring.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import verbly.spring.domain.chat.service.ChatService;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/chatrooms")
public class ChatController {

    private final ChatService chatService;

    // 메세지 전송-저장
    // 사용자의 전체 채팅방 조회
    // 전체 채팅 조회
    // 특정 채팅방 검색
    // 특정 메세지 검색
    // 채팅방 나가기


}
