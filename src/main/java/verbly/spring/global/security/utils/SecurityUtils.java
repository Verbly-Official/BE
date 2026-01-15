package verbly.spring.global.security.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import verbly.spring.global.security.auth.CustomUserDetails;

public class SecurityUtils {
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("사용자 인증 정보가 존재하지 않습니다.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUserId();
        }

        throw new IllegalStateException("인증 정보가 UserDetails 타입이 아닙니다.");
    }
}
