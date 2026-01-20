package verbly.spring.domain.user.enums;

public enum UserStatus {
    NEED_ONBOARDING, // 소셜 로그인 직후
    ACTIVE, // 온보딩 완료
    SUSPENDED,
    DELETED
}
