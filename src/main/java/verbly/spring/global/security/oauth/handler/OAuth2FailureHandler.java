package verbly.spring.global.security.oauth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import verbly.spring.global.common.utils.CookieUtils;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2FailureHandler implements AuthenticationFailureHandler {
    private final CookieUtils cookieUtils;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.error("❌ OAuth2 로그인 실패: {}", exception.getMessage());

        // JSON 응답 방식
//        response.setContentType("application/json;charset=UTF-8");
//        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//        response.getWriter().write("{\"success\": false, \"message\": \"" + exception.getMessage() + "\"}");

        /**/
        // 쿠키로 프론트에게 내려주기
        // 1. 실패 상태 쿠키 설정 (HttpOnly false → JS에서 읽음)
        cookieUtils.addCookie(response, "isSuccess", "false", false, 60);
        cookieUtils.addCookie(response, "code", "AUTH_FAILURE", false, 60);  // 필요 시 고유 코드로 변경 가능
        cookieUtils.addCookie(response, "message", java.net.URLEncoder.encode(exception.getMessage(), java.nio.charset.StandardCharsets.UTF_8), false, 60);

        cookieUtils.clearJsessionCookie(response);

        // 2. 실패용 브릿지 페이지로 리다이렉트
        response.sendRedirect("http://localhost:5173/login/callback"); // https://www.verbly.kr/login/callback

    }
}
