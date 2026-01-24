package verbly.spring.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import verbly.spring.domain.chat.entity.ChatMessage;
import verbly.spring.domain.chat.entity.Chatroom;
import verbly.spring.domain.chat.repo.ChatroomRepository;
import verbly.spring.domain.chat.repo.ChatroomUserRepository;
import verbly.spring.domain.chat.repo.ChatMessageRepository;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.repository.UserRepository;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.webSocket.exception.WebSocketExceptionHandler;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ChatMessageService {

    private final ChatroomService chatroomService;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;

    //채팅 저장
    public void saveChatMessage(Long senderId, Long chatroomId, String text) {

        Optional<User> optionalSender = userRepository.findById(senderId);
        if(optionalSender.isEmpty())
            throw new WebSocketExceptionHandler(ErrorStatus.USER_NOT_FOUND);
        User sender = optionalSender.get();

        // chatroom
        Chatroom chatroom = chatroomService.getChatroom(chatroomId);

        // chat
        ChatMessage chatMessage = ChatMessage.of(sender, chatroom, text);

        // save
        chatMessageRepository.save(chatMessage);
    }

    // 채팅방 참여-ws

    // 사용자의 전체 채팅방 조회

    // 전체 채팅 조회

    // 특정 채팅방 검색

    // 특정 메세지 검색

    // 채팅방 나가기

}
