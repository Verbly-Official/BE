package verbly.spring.domain.chat.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import verbly.spring.domain.chat.dto.responseDTO.ChatMessageResponseDTO;
import verbly.spring.domain.chat.dto.responseDTO.ChatProfileResponseDTO;
import verbly.spring.domain.chat.dto.responseDTO.OuterChatroomInfoResponseDTO;
import verbly.spring.domain.chat.dto.responseDTO.SearchResponseDTO;
import verbly.spring.domain.chat.entity.ChatMessage;
import verbly.spring.domain.chat.entity.Chatroom;
import verbly.spring.domain.chat.entity.ChatroomUser;
import verbly.spring.domain.chat.exception.ChatHandler;
import verbly.spring.domain.chat.repo.ChatMessageRepository;
import verbly.spring.domain.chat.repo.ChatroomUserRepository;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.repository.UserRepository;
import verbly.spring.global.common.code.ErrorStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatIntegralService {

    private final ChatMessageRepository  chatMessageRepository;
    private final ChatroomUserRepository chatroomUserRepository;
    private final UserRepository userRepository;

    // search ChatMeessage or enteredChatroom
    @Transactional
    public SearchResponseDTO getSearchResult(Long userId, String search) {

        // get profile by search
        List<ChatroomUser> chatroomUserList = chatroomUserRepository.findBySearch(userId, search);
        List<ChatProfileResponseDTO> chatProfileResponseDTOList = new ArrayList<>();
        for(ChatroomUser chatroomUser : chatroomUserList){
            Optional<User> optionalOpponent = userRepository.findById(chatroomUser.getOpponentId());
            if(optionalOpponent.isPresent()){
                chatProfileResponseDTOList.add(ChatProfileResponseDTO.from(chatroomUser.getChatroom().getId(), optionalOpponent.get()));
            }
        }

        // get chatroom by search
        List<ChatMessage> chatMessageList = chatMessageRepository.findBySearch(userId, search);
        List<OuterChatroomInfoResponseDTO> outerChatroomInfoResponseDTOList = new ArrayList<>();
        for(ChatMessage chatMessage : chatMessageList){
            Chatroom chatroom = chatMessage.getChatroom();
            Optional<ChatroomUser> optionalChatroomUser = chatroomUserRepository.findByChatroomIdAndUserId(chatroom.getId(), userId);
            if(optionalChatroomUser.isEmpty())
                throw new ChatHandler(ErrorStatus.NOT_CHATROOM_MEMBER);

            ChatroomUser chatroomUser = optionalChatroomUser.get();

            Optional<User> optionalOpponent = userRepository.findById(chatroomUser.getOpponentId());
            User opponent = null;
            if(optionalOpponent.isPresent())
                opponent = optionalOpponent.get();

            Integer unreadChatCount = chatMessageRepository.countUnreadChatMessage(userId, chatroomUser.getChatroom().getId(), chatroomUser.getLastReadAt());

            outerChatroomInfoResponseDTOList.add(OuterChatroomInfoResponseDTO.from(chatroomUser, opponent, chatMessage, unreadChatCount));
        }

        return SearchResponseDTO.from(chatProfileResponseDTOList, outerChatroomInfoResponseDTOList);
    }
}
