package verbly.spring.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import verbly.spring.domain.auth.dto.response.AuthResponseDTO;
import verbly.spring.domain.auth.exception.AuthHandler;
import verbly.spring.domain.auth.service.AuthCommandService;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.common.code.SuccessStatus;
import verbly.spring.global.common.response.ApiResponse;
import verbly.spring.global.security.jwt.JwtTokenProvider;
import verbly.spring.global.security.utils.SecurityUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/auth")
public class AuthRestController {
    private final AuthCommandService authCommandService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/logout")
    @Operation(summary = "회원 로그아웃 API - JWT AccessToken 인증 필요",
            description = "JWT 인증된 유저가 로그아웃하는 API입니다.",
            security = { @SecurityRequirement(name = "JWT TOKEN") }
    )
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        Long userId = SecurityUtils.getCurrentUserId();
        authCommandService.logout(response, userId);
        return ResponseEntity.status(SuccessStatus.USER_LOGOUT_SUCCESS.getHttpStatus()).body(ApiResponse.of(SuccessStatus.USER_LOGOUT_SUCCESS, null));
    }

    @PostMapping("/reissue")
    @Operation(summary = "토큰 재발급 API - JWT RefreshToken 인증 필요",
            description = "RefreshToken으로 AccessToken을 재발급받는 API입니다. AccessToken은 HttpOnly 쿠키로 내려줍니다."
    )
//    public ApiResponse<AuthResponseDTO.ReissueTokenResponseDTO> reissueAccessToken(HttpServletRequest request) {
//        String refreshToken = JwtTokenProvider.resolveToken(request); // Authorization 헤더에서 Bearer 토큰을 추출
    public void reissueAccessToken(HttpServletRequest request, HttpServletResponse response) {
//    public ApiResponse<AuthResponseDTO.ReissueTokenResponseDTO> reissueAccessToken(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = jwtTokenProvider.resolveRefreshToken(request);
        if (!StringUtils.hasText(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) { // refreshToken == null은 !StringUtils.hasText(refreshToken)로 체크 가능
            throw new AuthHandler(ErrorStatus.INVALID_JWT_REFRESH_TOKEN); // TOKEN4002
        }

        authCommandService.reissue(response, refreshToken);

//        return ApiResponse.onSuccess(tokens); // 테스트용
        // 응답 바디 없이 204 No Content
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
