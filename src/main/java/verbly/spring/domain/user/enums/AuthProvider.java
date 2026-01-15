package verbly.spring.domain.user.enums;

public enum AuthProvider {
    KAKAO, GOOGLE;

    public static AuthProvider from(String value) {
        return switch (value.toLowerCase()) {
            case "kakao" -> KAKAO;
            case "google" -> GOOGLE;
            default -> throw new IllegalArgumentException("지원하지 않는 provider입니다: " + value);
        };
    }
}
