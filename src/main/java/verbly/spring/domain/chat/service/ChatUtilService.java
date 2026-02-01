package verbly.spring.domain.chat.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import verbly.spring.domain.chat.entity.Chatroom;
import verbly.spring.domain.chat.entity.ChatroomUser;
import verbly.spring.domain.chat.repo.ChatroomRepository;
import verbly.spring.domain.chat.repo.ChatroomUserRepository;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ChatUtilService {

    private final ChatroomUserRepository chatroomUserRepository;

    // member check
    @Transactional
    public boolean isChatroomMember(Long roomId, Long userId) {

        return chatroomUserRepository.existsByChatroomIdAndUserId(roomId, userId);
    }

    // entered chatroom exist check, if exist => return chatroom
    @Transactional
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
