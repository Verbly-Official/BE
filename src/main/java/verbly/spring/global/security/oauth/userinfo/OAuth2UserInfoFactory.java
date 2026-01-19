package verbly.spring.global.security.oauth.userinfo;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.util.Map;

public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(String provider, Map<String, Object> attributes) {
        return switch (provider.toLowerCase()) {
            case "kakao" -> new KakaoUserInfo(attributes);
            case "google" -> new GoogleUserInfo(attributes);
            default -> throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다.");
        };
    }
}
