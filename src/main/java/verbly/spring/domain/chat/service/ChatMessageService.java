package verbly.spring.domain.chat.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import verbly.spring.domain.chat.dto.responseDTO.ChatMessageResponseDTO;
import verbly.spring.domain.chat.entity.ChatMessage;
import verbly.spring.domain.chat.entity.Chatroom;
import verbly.spring.domain.chat.entity.ChatroomUser;
import verbly.spring.domain.chat.exception.ChatHandler;
import verbly.spring.domain.chat.repo.ChatMessageRepository;
import verbly.spring.domain.chat.repo.ChatroomUserRepository;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.repository.UserRepository;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.webSocket.exception.WebSocketExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ChatMessageService {

    private final ChatroomService chatroomService;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatroomUserRepository chatroomUserRepository;

    //save chatMessage
    @Transactional
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
    @Transactional
    public List<ChatMessageResponseDTO> getChatMessageList(Long participantId, Long chatroomId) {

        Optional<ChatroomUser> optionalChatroomUser = chatroomUserRepository.findByChatroomIdAndUserId(chatroomId, participantId);
        if(optionalChatroomUser.isEmpty())
            throw new ChatHandler(ErrorStatus.USER_NOT_FOUND);
        ChatroomUser chatroomUser = optionalChatroomUser.get();

        List<ChatMessage> chatMessageList = chatMessageRepository.findAllBySenderIdAndChatroomIdOrderByCreatedAtDesc(participantId, chatroomId);
        List<ChatMessageResponseDTO> chatMessageResponseDTOList = chatMessageList
                .stream()
                .map(ChatMessageResponseDTO::from)
                .toList();

        LocalDateTime lastReadMessageTime = chatMessageList.get(chatMessageList.size() -1).getCreatedAt();
        if (lastReadMessageTime.isAfter(chatroomUser.getLastReadAt())) {
            chatroomUser.updateLastReadAt(lastReadMessageTime);
        }

        return chatMessageResponseDTOList;
    }


}
