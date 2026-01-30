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


}
