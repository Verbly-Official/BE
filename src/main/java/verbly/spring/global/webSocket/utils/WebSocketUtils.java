package verbly.spring.global.webSocket.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import verbly.spring.domain.chat.entity.ChatroomUser;
import verbly.spring.domain.chat.repo.ChatroomUserRepository;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.repository.UserRepository;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.webSocket.exception.WebSocketExceptionHandler;

import java.net.URI;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class WebSocketUtils {

    private final UserRepository userRepository;
    private final ChatroomUserRepository chatroomUserRepository;

    public Long getChatroomIdByURI(URI uri) {

        String path = uri.getPath();
        if ((path == null) || (path.isEmpty())) {
            throw new WebSocketExceptionHandler(ErrorStatus.URI_PATH_NOT_FOUND);
        }
        String[] uriPart = path.split("/");

        return Long.parseLong(uriPart[uriPart.length - 1]);
    }

    public Long getChatroomIdBySession(WebSocketSession session) {

        Object objChatroomId = session.getAttributes().get("chatroomId");
        if (objChatroomId == null)
            throw new WebSocketExceptionHandler(ErrorStatus.CHATROOM_NOT_FOUND);

        return Long.parseLong(objChatroomId.toString());
    }

    public Long getIdBySession(WebSocketSession session) {

        Object objUserId = session.getAttributes().get("userId");
        if (objUserId == null)
            throw new WebSocketExceptionHandler(ErrorStatus.USER_NOT_FOUND);

        return Long.parseLong(objUserId.toString());
    }

    // refactor soon - move to UserService
    public Long getUserIdBySocialId(String socialId) {

        Optional<User> optionalUser = userRepository.findBySocialId(socialId);
        if(optionalUser.isEmpty())
            throw new WebSocketExceptionHandler(ErrorStatus.USER_NOT_FOUND);
        User user = optionalUser.get();

        return user.getId();
    }

    public ChatroomUser getChatroomUser(WebSocketSession session) {

        Long userId = getIdBySession(session);
        Long chatroomId = getChatroomIdBySession(session);
        Optional<ChatroomUser> chatroomUser = chatroomUserRepository.findByChatroomIdAndUserId(chatroomId, userId);
        if (chatroomUser.isEmpty())
            throw new WebSocketExceptionHandler(ErrorStatus.USER_NOT_FOUND);

        return chatroomUser.get();
    }
}
