package verbly.spring.domain.auth.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import verbly.spring.domain.auth.converter.AuthConverter;
import verbly.spring.domain.auth.dto.response.AuthResponseDTO;
import verbly.spring.domain.auth.exception.AuthHandler;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.exception.UserHandler;
import verbly.spring.domain.user.repository.UserRepository;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.common.utils.CookieUtils;
import verbly.spring.global.security.auth.CustomUserDetails;
import verbly.spring.global.security.jwt.JwtTokenProvider;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthCommandServiceImpl implements AuthCommandService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtils cookieUtils;

    @Override
    public void logout(HttpServletResponse response, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // JWT를 로컬(localStorage, 쿠키 등)에서 직접 제거해야 로그아웃
        cookieUtils.clearCookie(response, "accessToken", "", false);

        // refreshToken 쿠키 삭제 (즉시 만료 설정)
        cookieUtils.clearCookie(response, "refreshToken", "", false);

        log.info("유저 {} 로그아웃 처리 및 JWT 쿠키 삭제 완료", user.getId());
    }

    @Override
    public AuthResponseDTO.ReissueTokenResponseDTO reissue(HttpServletResponse response, String refreshToken) {
        // 1. RefreshToken 유효성 검사 (서명 및 토큰 타입까지 확인)
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new AuthHandler(ErrorStatus.INVALID_JWT_REFRESH_TOKEN);
        }

        // 2. RefreshToken에서 subject(socialId) 추출
        String socialId = jwtTokenProvider.getSubjectFromToken(refreshToken);

        // 3. DB에서 해당 유저 조회 (예외 처리 포함)
        User user = userRepository.findBySocialId(socialId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        // 4. AccessToken 새로 생성 (Authentication 객체 없이도 직접 생성 가능)
//        String newAccessToken = jwtTokenProvider.generateAccessTokenFromSocialId(socialId);
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                new UsernamePasswordAuthenticationToken(
//                        socialId,
//                        null,
//                        user.getAuthorities()
                        customUserDetails.getUsername(), // socialId
                        null,
                        customUserDetails.getAuthorities()
                )
        );

        cookieUtils.addCookie(response, "accessToken", newAccessToken, true, 60 * 60 * 4); // 4시간

        // 5. DTO 변환은 컨버터에 위임
        return AuthConverter.toReissueTokenResponseDTO(newAccessToken);
    }
}
