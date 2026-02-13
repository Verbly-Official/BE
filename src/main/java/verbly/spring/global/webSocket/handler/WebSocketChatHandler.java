package verbly.spring.global.webSocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import verbly.spring.domain.chat.entity.ChatroomUser;
import verbly.spring.domain.chat.repo.ChatroomUserRepository;
import verbly.spring.domain.chat.service.ChatUtilService;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.webSocket.exception.WebSocketExceptionHandler;
import verbly.spring.global.webSocket.utils.WebSocketUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketChatHandler extends TextWebSocketHandler {

    private final ChatroomUserRepository chatroomUserRepository;
    private final WebSocketUtils webSocketUtils;
    private final ChatUtilService chatUtilService;
    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<Long, Set<WebSocketSession>> nowChatroom = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception{

        /*
        1. get chatroomId from session
        2. get userId from session
        3. add session
            - key=chatroomId : value=Set<session>
        */

        Long chatroomId = webSocketUtils.getChatroomIdBySession(session);
        Long userId = webSocketUtils.getIdBySession(session);

        // member check
        if(!chatUtilService.isChatroomMember(chatroomId, userId)) {
            session.close();
            return;
        }

        // key contains check
        if(!nowChatroom.containsKey(chatroomId))
            nowChatroom.put(chatroomId, ConcurrentHashMap.newKeySet());

        if(!session.isOpen())
            throw new WebSocketExceptionHandler(ErrorStatus.WEBSOCKET_SESSION_CLOSED);

        nowChatroom.computeIfAbsent(chatroomId, k -> ConcurrentHashMap.newKeySet())
                .add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception{

        /*
        1. get chatroomId
        2. get Set<session> where {chatroomId} is the key
        3. remove closed session
        4. remove if empty
         */

        ChatroomUser chatroomUser = webSocketUtils.getChatroomUser(session);
        chatroomUser.updateLastReadAt(LocalDateTime.now());
        chatroomUserRepository.save(chatroomUser);

        Long  chatroomId =  webSocketUtils.getChatroomIdBySession(session);

        nowChatroom.computeIfPresent(chatroomId, (id, sessions) -> {
           sessions.remove(session);
           return sessions.isEmpty() ? null : sessions;
        });
    }

    public void broadcastMessage(Long chatroomId, Object payload) {

        Set<WebSocketSession> nowChatroomUser = nowChatroom.get(chatroomId);
        if(nowChatroomUser == null || nowChatroomUser.isEmpty())
            return;

        final String text;

        try {
            text = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new WebSocketExceptionHandler(ErrorStatus.JSON_PROCESS_FAIL);
        }

        TextMessage textMessage = new TextMessage(text);

        try {
            for (WebSocketSession webSocketSession : nowChatroomUser) {
                if (webSocketSession == null || webSocketSession.isOpen()) {
                        webSocketSession.sendMessage(textMessage);
                }
            }
        }  catch (Exception e) {
            throw new WebSocketExceptionHandler(ErrorStatus.JSON_PROCESS_FAIL);
        }

        if (nowChatroomUser.isEmpty())
            nowChatroom.remove(chatroomId);
    }
}
