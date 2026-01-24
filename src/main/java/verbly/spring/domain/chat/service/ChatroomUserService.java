package verbly.spring.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import verbly.spring.domain.chat.repo.ChatroomUserRepository;

@RequiredArgsConstructor
@Service
public class ChatroomUserService {

    private final ChatroomUserRepository chatroomUserRepository;

    public boolean isChatroomMember(Long roomId, Long userId) {

        return chatroomUserRepository.existsByChatroomIdAndUserIdAndQuitAtIsNull(roomId, userId);
    }

}
