package verbly.spring.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.stereotype.Component;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.common.dto.ErrorReasonDTO;
import verbly.spring.global.common.response.ApiResponse;

import java.io.IOException;

@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler { // 인증은 되었으나 권한이 없을 때의 403 에러를 반환
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        ErrorReasonDTO reason;

        reason = ErrorReasonDTO.builder()
                .httpStatus(ErrorStatus.ACCESS_DENIED.getHttpStatus())
                .isSuccess(false)
                .code(ErrorStatus.ACCESS_DENIED.getCode())
                .message(ErrorStatus.ACCESS_DENIED.getMessage())
                .build();

        log.warn("❗ AccessDeniedException 발생 - 클래스: {}", accessDeniedException.getClass().getName());
        log.warn("❗ AccessDeniedException 메시지: {}", accessDeniedException.getMessage());
        log.error("❌ [AccessDenied] URI: {}, Method: {}", request.getRequestURI(), request.getMethod());

        response.getWriter().write(new ObjectMapper().writeValueAsString(
                ApiResponse.onFailure(reason.getCode(), reason.getMessage(), reason)
        ));
    }
}
