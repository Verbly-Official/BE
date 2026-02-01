package verbly.spring.global.webSocket.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import verbly.spring.domain.chat.entity.ChatMessage;
import verbly.spring.domain.chat.entity.Chatroom;
import verbly.spring.domain.chat.entity.ChatroomUser;
import verbly.spring.domain.chat.exception.ChatHandler;
import verbly.spring.domain.chat.repo.ChatMessageRepository;
import verbly.spring.domain.chat.repo.ChatroomRepository;
import verbly.spring.domain.chat.repo.ChatroomUserRepository;
import verbly.spring.domain.chat.service.ChatMessageService;
import verbly.spring.domain.chat.service.ChatUtilService;
import verbly.spring.domain.chat.service.ChatroomService;
import verbly.spring.domain.chat.service.ChatroomUserService;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.repository.UserRepository;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.webSocket.exception.WebSocketExceptionHandler;
import verbly.spring.global.webSocket.util.WebSocketUtil;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketChatHandler extends TextWebSocketHandler {

    private final ChatMessageService chatMessageService;
    private final ChatroomUserRepository chatroomUserRepository;
    private final WebSocketUtil webSocketUtil;
    private final Map<Long, Set<WebSocketSession>> nowChatroom = new HashMap<>();
    private final ChatUtilService chatUtilService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception{

        /*
        1. get chatroomId from session
        2. get userId from session
        3. add session
            - key=chatroomId : value=Set<session>
        */

        Long chatroomId = webSocketUtil.getChatroomIdBySession(session);

        Long userId = webSocketUtil.getIdBySession(session);

        // member check
        if(!chatUtilService.isChatroomMember(chatroomId, userId)) {
            session.close();
            return;
        }

        // key contains check
        if(!nowChatroom.containsKey(chatroomId))
            nowChatroom.put(chatroomId, new HashSet<>());

        Set<WebSocketSession> nowChatroomSession = nowChatroom.get(chatroomId);
        if(!session.isOpen())
            throw new WebSocketExceptionHandler(ErrorStatus.WEBSOCKET_SESSION_CLOSED);
        nowChatroomSession.add(session);
        nowChatroom.put(chatroomId, nowChatroomSession);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        /*
        1. get message
        2. save message
        3. send
         */

        //message
        String text = message.getPayload();

        //sender
        Long senderId = webSocketUtil.getIdBySession(session);

        // chatroom
        Long  chatroomId = webSocketUtil.getChatroomIdBySession(session);

        // save
        chatMessageService.saveChatMessage(senderId, chatroomId, text);

        // send
        Set<WebSocketSession> nowChatroomUser = nowChatroom.get(chatroomId);
        for(WebSocketSession webSocketSession : nowChatroomUser){
            if(webSocketSession.isOpen()) {
                webSocketSession.sendMessage(new TextMessage(text));
                ChatroomUser chatroomUser = webSocketUtil.getChatroomUser(webSocketSession);
                chatroomUser.updateLastReadAt(LocalDateTime.now());
                chatroomUserRepository.save(chatroomUser);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception{

        /*
        1. get chatroomId
        2. get Set<session> where {chatroomId} is the key
        3. remove closed session
        4. remove if empty
         */
        Long  chatroomId =  webSocketUtil.getChatroomIdBySession(session);
        Set<WebSocketSession> nowChatroomUser = nowChatroom.get(chatroomId);
        nowChatroomUser.remove(session);

        if(nowChatroomUser.isEmpty())
            nowChatroom.remove(chatroomId);
    }
}
