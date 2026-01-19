package verbly.spring.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.common.dto.ErrorReasonDTO;
import verbly.spring.global.common.response.ApiResponse;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint { // 인증 실패 시 401 에러를 반환
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        ApiResponse<Object> body = ApiResponse.onFailure(ErrorStatus.INVALID_JWT_ACCESS_TOKEN);

        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
    }
}
