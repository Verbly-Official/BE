package verbly.spring.global.webSocket.util;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketSession;
import verbly.spring.domain.user.exception.UserHandler;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.common.constants.Constants;
import verbly.spring.global.webSocket.exception.WebSocketExceptionHandler;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@Component
public class WebSocketUtil {

    public Long getRoomId(WebSocketSession session) {
        URI uri = session.getUri();
        String path = uri.getPath();
        if ((path == null) || (path.isEmpty())) {
            throw new WebSocketExceptionHandler(ErrorStatus.URI_PATH_NOT_FOUND);
        }
        String[] uriPart = path.split("/");

        return Long.parseLong(uriPart[uriPart.length - 1]);
    }

    public String getSocialId(WebSocketSession session) {

        Object objSocialId = session.getAttributes().get("socialId");
        if (objSocialId == null)
            throw new UserHandler(ErrorStatus.USER_NOT_FOUND);

        return objSocialId.toString();
    }
}
