package verbly.spring.global.security.oauth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import verbly.spring.domain.auth.dto.response.AuthResponseDTO;
import verbly.spring.domain.user.entity.ProfileImage;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.enums.UserStatus;
import verbly.spring.global.common.code.SuccessStatus;
import verbly.spring.global.common.response.ApiResponse;
import verbly.spring.global.security.auth.CustomOAuth2User;
import verbly.spring.global.security.jwt.JwtTokenProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        log.info("✅ OAuth2 로그인 성공 핸들러 진입");
        log.info("🔐 authentication.getPrincipal() 타입: {}", authentication.getPrincipal().getClass().getName());

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        User user = oAuth2User.getUser();
        log.info("🙋‍♂️ 로그인한 유저 ID: {}, 온보딩 상태: {}", user.getId(), user.getStatus());

        // JWT 발급
        String accessToken = jwtTokenProvider.generateAccessToken(authentication); // kakao_12345
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getSocialId());

        log.info("🔑 AccessToken: {}, RefreshToken: {}", accessToken, refreshToken);

        String nickname = Optional.ofNullable(user.getNickname()).orElse("");
        String profileImageUrl = Optional.ofNullable(user.getProfileImage())
                .map(ProfileImage::getImageUrl)
                .orElse("default_profile_url");
        String email = Optional.ofNullable(user.getEmail()).orElse("");

        /*
        // JSON 응답 방식 (SPA 등 API 호출용)
        AuthResponseDTO.LoginResultDTO result = AuthResponseDTO.LoginResultDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .provider(user.getProvider().toString())
                .nickname(nickname)
                .profileImage(profileImageUrl)
                .email(email)
                .status(user.getStatus().name())
                .build();

        ApiResponse<AuthResponseDTO.LoginResultDTO> apiResponse;
        if (user.getStatus() == UserStatus.ACTIVE) {
            apiResponse = ApiResponse.of(SuccessStatus.USER_ALREADY_LOGIN, result);
        } else {
            apiResponse = ApiResponse.of(SuccessStatus.USER_NEEDS_ONBOARDING, result);
        }

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        */

        /**/
        // 쿠키로 프론트에게 내려주기
        boolean isOnboardingCompleted = user.getStatus() == UserStatus.ACTIVE; // ACTIVE = 소셜 가입 완료, 온보딩 전
        SuccessStatus status = isOnboardingCompleted
                ? SuccessStatus.USER_ALREADY_LOGIN
                : SuccessStatus.USER_NEEDS_ONBOARDING;

        // 1. 민감 정보: HttpOnly + Secure 쿠키
        addCookie(response, "accessToken", accessToken, true, 60 * 60 * 4); // 4시간
        addCookie(response, "refreshToken", refreshToken, true, 60 * 60 * 24 * 7); // 7일
        addCookie(response, "userId", String.valueOf(user.getId()), true, 60 * 60 * 4);
        addCookie(response, "provider", user.getProvider().toString(), false, 60 * 60 * 4);
        addCookie(response, "nickname", nickname, false, 60 * 60 * 4);
        addCookie(response, "profileImage", profileImageUrl, false, 60 * 60 * 4);
        addCookie(response, "email", email, false, 60 * 60 * 4);
        addCookie(response, "userStatus", String.valueOf(user.getStatus()), false, 60 * 60 * 4);

        // 2. 상태 정보: HttpOnly = false (JS에서 읽게)
        addCookie(response, "isSuccess", "true", false, 60);
        addCookie(response, "code", status.getCode(), false, 60);
        addCookie(response, "message", URLEncoder.encode(status.getMessage(), StandardCharsets.UTF_8), false, 60);

        clearJsessionCookie(response);

        // 3. 리다이렉트 (브릿지 페이지)
        response.sendRedirect("http://localhost:5173/login/callback"); // https://www.verbly.kr/login/callback

    }

    private void addCookie(HttpServletResponse response, String name, String value, boolean httpOnly, int maxAgeInSeconds) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(httpOnly)
                .secure(true) // 운영환경에서는 true (HTTPS)
                .path("/")
                .domain("localhost") // www.verbly.kr
                .maxAge(maxAgeInSeconds)
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    private void clearJsessionCookie(HttpServletResponse response) {
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
