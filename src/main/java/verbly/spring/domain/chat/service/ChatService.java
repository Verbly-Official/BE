package verbly.spring.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import verbly.spring.domain.chat.repo.ChatroomRepository;
import verbly.spring.domain.chat.repo.ChatroomUserRepository;
import verbly.spring.domain.chat.repo.MessageRepository;

@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChatroomRepository chatroomRepository;
    private final ChatroomUserRepository chatroomUserRepository;
    private final MessageRepository messageRepository;
}
