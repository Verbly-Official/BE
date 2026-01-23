package verbly.spring.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import verbly.spring.domain.chat.entity.ChatMessage;
import verbly.spring.domain.chat.repo.ChatroomRepository;
import verbly.spring.domain.chat.repo.ChatroomUserRepository;
import verbly.spring.domain.chat.repo.ChatMessageRepository;

@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChatroomRepository chatroomRepository;
    private final ChatroomUserRepository chatroomUserRepository;
    private final ChatMessageRepository chatMessageRepository;

    // 채팅방 참여-ws

    // 사용자의 전체 채팅방 조회

    // 전체 채팅 조회

    // 특정 채팅방 검색

    // 특정 메세지 검색

    // 채팅방 나가기

}
