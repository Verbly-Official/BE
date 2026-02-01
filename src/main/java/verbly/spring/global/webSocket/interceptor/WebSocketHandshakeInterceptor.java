package verbly.spring.global.webSocket.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import verbly.spring.domain.chat.service.ChatUtilService;
import verbly.spring.domain.chat.service.ChatroomUserService;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.exception.UserHandler;
import verbly.spring.domain.user.repository.UserRepository;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.common.constants.Constants;
import verbly.spring.global.security.auth.CustomUserDetails;
import verbly.spring.global.security.jwt.JwtTokenProvider;
import verbly.spring.global.webSocket.exception.WebSocketExceptionHandler;
import verbly.spring.global.webSocket.util.WebSocketUtil;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final WebSocketUtil webSocketUtil;
    private final ChatUtilService chatUtilService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        // Header deliver check - delete soon
        log.info("Auth header: {}", request.getHeaders().getFirst(Constants.AUTH_HEADER));

        // token validation check
        List<String> auth = request.getHeaders().get(Constants.AUTH_HEADER);
        if (auth == null || auth.isEmpty())
            throw new WebSocketExceptionHandler(ErrorStatus._UNAUTHORIZED);
        String bearerToken = auth.get(0);
        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith(Constants.TOKEN_PREFIX))
            throw new WebSocketExceptionHandler(ErrorStatus.INVALID_JWT_ACCESS_TOKEN);
        String token = bearerToken.substring(Constants.TOKEN_PREFIX.length()).trim();
        if (!StringUtils.hasText(token))
            throw new WebSocketExceptionHandler(ErrorStatus.INVALID_JWT_ACCESS_TOKEN);

        // get userId from socialId in JWT
        Long userId = webSocketUtil.getUserIdBySocialId(jwtTokenProvider.getSubjectFromToken(token));

        // get URI
        URI uri = request.getURI();
        Long chatroomId = webSocketUtil.getChatroomIdByURI(uri);

        // member check
        if(!chatUtilService.isChatroomMember(chatroomId, userId))
            throw new WebSocketExceptionHandler(ErrorStatus.NOT_CHATROOM_MEMBER);

        // save in websocketSession
        attributes.put("userId", userId);
        attributes.put("chatroomId", chatroomId);

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // after handshake
    }
}
