package verbly.spring.domain.chat.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import verbly.spring.domain.chat.entity.Chatroom;
import verbly.spring.domain.chat.exception.ChatHandler;
import verbly.spring.domain.chat.repo.ChatroomRepository;
import verbly.spring.global.common.code.ErrorStatus;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatroomService {

    private final ChatroomRepository chatroomRepository;

    @Transactional
    public Chatroom getChatroom(Long roomId) {

        Optional<Chatroom> optionalChatroom = chatroomRepository.findById(roomId);
        if(optionalChatroom.isEmpty())
            throw new ChatHandler(ErrorStatus.CHATROOM_NOT_FOUND);

        return optionalChatroom.get();
    }
}
