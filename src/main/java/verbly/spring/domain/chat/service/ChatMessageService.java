package verbly.spring.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import verbly.spring.domain.chat.dto.responseDTO.ChatMessageResponseDTO;
import verbly.spring.domain.chat.entity.ChatMessage;
import verbly.spring.domain.chat.entity.Chatroom;
import verbly.spring.domain.chat.repo.ChatroomRepository;
import verbly.spring.domain.chat.repo.ChatroomUserRepository;
import verbly.spring.domain.chat.repo.ChatMessageRepository;
import verbly.spring.domain.review.dto.responeDTO.ReviewResponseDTO;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.repository.UserRepository;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.webSocket.exception.WebSocketExceptionHandler;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ChatMessageService {

    private final ChatroomService chatroomService;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;

    //save chatMessage
    public void saveChatMessage(Long senderId, Long chatroomId, String text) {

        // sender
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

    // get certain room messages
    public List<ChatMessageResponseDTO> getChatMessageList(Long participantId, Long chatroomId) {

        List<ChatMessage> chatMessageList = chatMessageRepository.findAllBySenderIdAndChatroomId(participantId, chatroomId);

        return chatMessageList
                .stream()
                .map(ChatMessageResponseDTO::from)
                .toList();
    }


}
