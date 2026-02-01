package verbly.spring.domain.chat.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import verbly.spring.domain.chat.dto.responseDTO.ChatroomEnterResponseDTO;
import verbly.spring.domain.chat.dto.responseDTO.InnerChatroomInfoResponseDTO;
import verbly.spring.domain.chat.dto.responseDTO.OuterChatroomInfoResponseDTO;
import verbly.spring.domain.chat.entity.ChatMessage;
import verbly.spring.domain.chat.entity.Chatroom;
import verbly.spring.domain.chat.entity.ChatroomUser;
import verbly.spring.domain.chat.exception.ChatHandler;
import verbly.spring.domain.chat.repo.ChatMessageRepository;
import verbly.spring.domain.chat.repo.ChatroomRepository;
import verbly.spring.domain.chat.repo.ChatroomUserRepository;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.repository.UserRepository;
import verbly.spring.global.common.code.ErrorStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ChatroomUserService {

    private final UserRepository userRepository;
    private final ChatroomUserRepository chatroomUserRepository;
    private final ChatroomRepository chatroomRepository;
    private final ChatUtilService chatUtilService;
    private final ChatMessageRepository chatMessageRepository;

    // enter chatroom
    @Transactional
    public ChatroomEnterResponseDTO enterChatroom(Long participantId, Long opponentId) {

        if(participantId.equals(opponentId))
            throw new ChatHandler(ErrorStatus.CANT_SELF_CHAT);

        // chatroom exist check - create new
        Chatroom chatroom = chatUtilService.isEnteredChatroomExist(participantId, opponentId);
        if(chatroom == null) {
            chatroom = Chatroom.of();
            chatroomRepository.save(chatroom);
        }

        // memberCheck - create new
        notMemberThenRegister(chatroom, participantId, opponentId);

        // opposite memberCheck  - create new
        notMemberThenRegister(chatroom, opponentId, participantId);

        return ChatroomEnterResponseDTO.from(chatroom);
    }

    // no search => order By latest chatted chatroom
    // chatroom name= opponent name, thumbnail = opponent image url
    @Transactional
    public List<OuterChatroomInfoResponseDTO> getChatroomInfoList(Long participantId) {

        List<OuterChatroomInfoResponseDTO> outerChatroomInfoResponseDTOList = new ArrayList<>();

        List<ChatroomUser> chatroomUserList = chatroomUserRepository.findAllByUserId(participantId);

        for (ChatroomUser chatroomUser : chatroomUserList) {
            // get recent message
            Long chatroomId = chatroomUser.getChatroom().getId();
            Optional<ChatMessage> optionalChatMessage = chatMessageRepository.findTopByChatroomIdOrderByCreatedAtDesc(chatroomId);
            ChatMessage chatMessage;

            // only get message exists chatroom
            if(optionalChatMessage.isPresent()) {
                chatMessage = optionalChatMessage.get();

                // get chatroom info
                Optional<User> optionalOpponent = userRepository.findById(chatroomUser.getOpponentId());
                User opponent = null;
                if (optionalOpponent.isPresent()) {
                    opponent = optionalOpponent.get();
                }

                Integer unreadChatCount = chatMessageRepository.countUnreadChatMessage(participantId, chatroomUser.getChatroom().getId(), chatroomUser.getLastReadAt());

                outerChatroomInfoResponseDTOList.add(OuterChatroomInfoResponseDTO.from(chatroomUser, opponent, chatMessage, unreadChatCount));
            }
        }

        return outerChatroomInfoResponseDTOList;
    }

    // when entered, get entered chatroom info
    @Transactional
    public InnerChatroomInfoResponseDTO getChatroomInfo(Long participantId, Long opponentId) {

        //participant
        Optional<ChatroomUser> optionalChatroomUser = chatroomUserRepository.findByUserIdAndOpponentId(participantId, opponentId);
        if(optionalChatroomUser.isEmpty())
            throw new ChatHandler(ErrorStatus.NOT_CHATROOM_MEMBER);
        ChatroomUser chatroomUser = optionalChatroomUser.get();

        //opponent
        Optional<User> optionalOpponent = userRepository.findById(opponentId);
        if (optionalOpponent.isEmpty())
            throw new ChatHandler(ErrorStatus.USER_NOT_FOUND);
        User opponent = optionalOpponent.get();

        return  InnerChatroomInfoResponseDTO.from(chatroomUser, opponent);
    }

    // quit chatroom
    @Transactional
    public void quitChatroom(Long participantId, Long opponentId) {

        Optional<ChatroomUser> optionalChatroomUser = chatroomUserRepository.findByUserIdAndOpponentId(participantId, opponentId);
        if(optionalChatroomUser.isEmpty())
            throw new ChatHandler(ErrorStatus.NOT_CHATROOM_MEMBER);
        ChatroomUser chatroomUser = optionalChatroomUser.get();

        chatroomUserRepository.delete(chatroomUser);
    }

    @Transactional
    private void notMemberThenRegister(Chatroom chatroom, Long participantId, Long opponentId) {

        Optional<ChatroomUser> optionalChatroomUser = chatroomUserRepository.findByUserIdAndOpponentId(participantId, opponentId);
        // already member => pass
        if(optionalChatroomUser.isPresent())
            return;

        // not member => register
        Optional<User> optionalParticipant = userRepository.findById(participantId);
        if(optionalParticipant.isEmpty())
            throw new ChatHandler(ErrorStatus.USER_NOT_FOUND);
        User participant = optionalParticipant.get();

        ChatroomUser chatroomParticipant = ChatroomUser.of(participant, chatroom, opponentId);
        chatroomUserRepository.save(chatroomParticipant);
    }

}
