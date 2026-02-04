package verbly.spring.global.common.utils;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CookieUtils {
    public void addCookie(HttpServletResponse response, String name, String value, boolean httpOnly, int maxAgeInSeconds) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(httpOnly)
                .secure(true) // 운영환경에서는 true (HTTPS)
                .path("/")
//                .domain("www.verbly.kr")
                .maxAge(maxAgeInSeconds)
                .sameSite("None")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        log.info("쿠키 생성 완료: {}", name);
    }

    public void clearCookie(HttpServletResponse response, String name, String value, boolean httpOnly) {
        ResponseCookie deleteTokenCookie = ResponseCookie.from(name, value)
                .httpOnly(httpOnly)
                .secure(true)
                .path("/")
//                .domain("www.verbly.kr")
                .maxAge(0)
                .sameSite("None") // Lax
                .build();

        response.addHeader("Set-Cookie", deleteTokenCookie.toString());
        log.info("쿠키 삭제 완료{}", name);
    }

    public void clearJsessionCookie(HttpServletResponse response) {
        ResponseCookie deleteJsessionCookie = ResponseCookie.from("JSESSIONID", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0) // 쿠키 즉시 만료
                .build();
        response.addHeader("Set-Cookie", deleteJsessionCookie.toString());
        log.info("JSESSIONID 쿠키 삭제 완료");
    }
}
