package verbly.spring.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import verbly.spring.domain.chat.dto.responseDTO.ChatroomInfoResponseDTO;
import verbly.spring.domain.chat.entity.Chatroom;
import verbly.spring.domain.chat.entity.ChatroomUser;
import verbly.spring.domain.chat.exception.ChatHandler;
import verbly.spring.domain.chat.repo.ChatroomRepository;
import verbly.spring.domain.chat.repo.ChatroomUserRepository;
import verbly.spring.domain.user.dto.response.UserResponseDTO;
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

    // enter chatroom
    public void enterChatroom(Long participantId, Long opponentId) {

        // chatroom exist check - create new
        Chatroom chatroom = isEnteredChatroomExist(participantId, opponentId);
        if(chatroom == null) {
            chatroom = Chatroom.of();
            chatroomRepository.save(chatroom);
        }

        // memberCheck - create new
        if(!isChatroomMember(chatroom.getId(), participantId)) {
            Optional<User> optionalParticipant = userRepository.findById(participantId);
            if(optionalParticipant.isEmpty())
                throw new ChatHandler(ErrorStatus.USER_NOT_FOUND);
            User participant = optionalParticipant.get();

            ChatroomUser chatroomParticipant = ChatroomUser.of(participant, chatroom, opponentId);
            chatroomUserRepository.save(chatroomParticipant);
        }

        // opposite memberCheck  - create new
        if(!isChatroomMember(chatroom.getId(), opponentId)) {
            Optional<User> optionalOpponent = userRepository.findById(opponentId);
            if(optionalOpponent.isEmpty())
                throw new ChatHandler(ErrorStatus.USER_NOT_FOUND);
            User opponent = optionalOpponent.get();

            ChatroomUser chatroomOpponent = ChatroomUser.of(opponent, chatroom, participantId);
            chatroomUserRepository.save(chatroomOpponent);
        }
    }

    // get entered chatroomList
    // chatroom name= opponent name, thumbnail = opponent image url
    public List<ChatroomInfoResponseDTO> getChatroomInfoList(Long participantId) {

        List<ChatroomInfoResponseDTO> chatroomInfoResponseDTOList = new ArrayList<>();

        List<ChatroomUser> chatroomUserList = chatroomUserRepository.findAllByUserId(participantId);

        for (ChatroomUser chatroomUser : chatroomUserList) {
            Optional<User> optionalOpponent = userRepository.findById(chatroomUser.getOpponentId());
            if (optionalOpponent.isEmpty())
                throw new ChatHandler(ErrorStatus.USER_NOT_FOUND);
            User opponent = optionalOpponent.get();

            chatroomInfoResponseDTOList.add(ChatroomInfoResponseDTO.from(chatroomUser, opponent));
        }

        return chatroomInfoResponseDTOList;
    }

    // get entered chatroom
    public ChatroomInfoResponseDTO getChatroomInfo(Long participantId, Long opponentId) {

        Optional<ChatroomUser> optionalChatroomUser = chatroomUserRepository.findByUserId(participantId);
        if(optionalChatroomUser.isEmpty())
            throw new ChatHandler(ErrorStatus.USER_NOT_FOUND);
        ChatroomUser chatroomUser = optionalChatroomUser.get();

        Optional<User> optionalOpponent = userRepository.findById(chatroomUser.getOpponentId());
        if (optionalOpponent.isEmpty())
            throw new ChatHandler(ErrorStatus.USER_NOT_FOUND);
        User opponent = optionalOpponent.get();

        return  ChatroomInfoResponseDTO.from(chatroomUser, opponent);
    }

    // quit chatroom
    public void quitChatroom(Long participantId) {

        Optional<ChatroomUser> optionalChatroomUser = chatroomUserRepository.findByUserId(participantId);
        if(optionalChatroomUser.isEmpty())
            throw new ChatHandler(ErrorStatus.USER_NOT_FOUND);
        ChatroomUser chatroomUser = optionalChatroomUser.get();

        chatroomUserRepository.delete(chatroomUser);
    }

    // member check
    public boolean isChatroomMember(Long roomId, Long userId) {

        return chatroomUserRepository.existsByChatroomIdAndUserId(roomId, userId);
    }

    // entered chatroom exist check
    protected Chatroom isEnteredChatroomExist(Long participantId, Long opponentId) {

        Optional<ChatroomUser> optionalChatroomUser = chatroomUserRepository.findByUserIdAndOpponentId(participantId, opponentId);
        if (optionalChatroomUser.isPresent())
            return optionalChatroomUser.get().getChatroom();

        Optional<ChatroomUser> optionalReverseChatroomUser = chatroomUserRepository.findByUserIdAndOpponentId(opponentId, participantId);
        if (optionalReverseChatroomUser.isPresent())
            return optionalReverseChatroomUser.get().getChatroom();

        return null;
    }

}
