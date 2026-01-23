package verbly.spring.global.webSocket.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.exception.UserHandler;
import verbly.spring.domain.user.repository.UserRepository;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.common.constants.Constants;
import verbly.spring.global.security.auth.CustomUserDetails;
import verbly.spring.global.security.jwt.JwtTokenProvider;
import verbly.spring.global.webSocket.exception.WebSocketExceptionHandler;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        //get token
        String token = JwtTokenProvider.resolveToken((HttpServletRequest)request);
        if (!StringUtils.hasText(token))
            throw new WebSocketExceptionHandler(ErrorStatus.INVALID_JWT_ACCESS_TOKEN);

        // get socialId from token
        String socialId = jwtTokenProvider.getSubjectFromToken(token);

        // save in websocketSession
        attributes.put("socialId", socialId);

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // after handshake
    }
}
