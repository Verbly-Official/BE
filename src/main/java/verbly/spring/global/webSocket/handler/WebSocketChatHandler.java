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
import verbly.spring.domain.chat.exception.ChatHandler;
import verbly.spring.domain.chat.repo.ChatMessageRepository;
import verbly.spring.domain.chat.repo.ChatroomRepository;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.repository.UserRepository;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.webSocket.exception.WebSocketExceptionHandler;
import verbly.spring.global.webSocket.util.WebSocketUtil;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketChatHandler extends TextWebSocketHandler {

    private final UserRepository userRepository;
    private final ChatroomRepository chatroomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final WebSocketUtil webSocketUtil;
    Map<Long, Set<WebSocketSession>> nowChatroom = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception{

        log.info("afterConnectionEstablished: {}", session.getId());

        Long roomId = webSocketUtil.getRoomId(session);
        log.info("eneteredRoomId: {}", roomId);

        nowChatroom.put(roomId, new HashSet<>());
        Set<WebSocketSession> nowChatroomUsers = nowChatroom.get(roomId);
        if(session.isOpen())
            nowChatroomUsers.add(session);
        else
            throw new WebSocketExceptionHandler(ErrorStatus.WEBSOCKET_SESSION_CLOSED);
        nowChatroom.put(roomId, nowChatroomUsers);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        //json message
        String text = message.getPayload();

        //sender
        String senderId = webSocketUtil.getSocialId(session);
        Optional<User> optionalSender = userRepository.findBySocialId(senderId);
        if(optionalSender.isEmpty())
            throw new WebSocketExceptionHandler(ErrorStatus.USER_NOT_FOUND);
        User sender = optionalSender.get();

        // chatroom
        Long  roomId = webSocketUtil.getRoomId(session);
        Optional<Chatroom> optionalChatroom = chatroomRepository.findById(roomId);
        if(optionalChatroom.isEmpty())
            throw new ChatHandler(ErrorStatus.CHATROOM_NOT_FOUND);
        Chatroom chatroom = optionalChatroom.get();

        // chat
        ChatMessage chatMessage = ChatMessage.of(sender, chatroom, text);

        // save
        chatMessageRepository.save(chatMessage);

        // send
        Set<WebSocketSession> nowChatroomUser = nowChatroom.get(roomId);
        for(WebSocketSession webSocketSession : nowChatroomUser){
            if(!webSocketSession.isOpen())
                throw new WebSocketExceptionHandler(ErrorStatus.WEBSOCKET_SESSION_CLOSED);
            webSocketSession.sendMessage(new TextMessage(text));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception{

        Long  roomId =  webSocketUtil.getRoomId(session);
        Set<WebSocketSession> nowChatroomUser = nowChatroom.get(roomId);
        nowChatroomUser.remove(session);

        log.info("afterConnectionClosed: {}", session.getId());
    }



}
