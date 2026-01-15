package verbly.spring.global.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import verbly.spring.global.common.constants.Constants;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter { // 필터 역할: 클라이언트가 서버에게 전달되기 전에 인증 과정

    private final JwtTokenProvider jwtTokenProvider;

//    private static final Set<String> NO_FILTER_URIS = Set.of("/auth/reissue", "/generate"); // /auth/reissue 외에도 필요하면

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        log.info("🔥 [JWT 필터 제외 검사]: {}", request.getRequestURI());
        return request.getRequestURI().equals("/api/auth/reissue");
//        return NO_FILTER_URIS.contains(request.getRequestURI()); // /auth/reissue 외에도 필요하면
//        return super.shouldNotFilter(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, // HttpServletRequest로 받아온 요청 객체에서 순수한 토큰을 추출
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if(StringUtils.hasText(token) &&
                jwtTokenProvider.validateToken(token) &&
                jwtTokenProvider.isAccessToken(token)) { // 순수한 토큰 검증 과정을 통과하면,
            Authentication authentication = jwtTokenProvider.getAuthentication(token); // 인증 객체를 만들고,
            SecurityContextHolder.getContext().setAuthentication(authentication); // 인증을 등록
        }
        filterChain.doFilter(request, response); // 다음 필터, 컨트롤러 등에 요청을 넘겨줌
    }

    private String resolveToken(HttpServletRequest request) { // 순수 토큰을 반환
        // 1. 우선 Authorization 헤더 확인
        String bearerToken = request.getHeader(Constants.AUTH_HEADER);
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith(Constants.TOKEN_PREFIX)) {
            return bearerToken.substring(Constants.TOKEN_PREFIX.length());
        }

        // 2. Authorization 헤더가 없다면 쿠키에서 accessToken 찾기
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
