package verbly.spring.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AuthResponseDTO {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResultDTO { // 소셜 로그인
        private String accessToken;
        private String refreshToken;
        private Long userId;
        private String status;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReissueTokenResponseDTO {
        private String accessToken;
    }
}
