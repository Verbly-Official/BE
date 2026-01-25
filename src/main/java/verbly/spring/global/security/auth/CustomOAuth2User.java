package verbly.spring.global.security.auth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import verbly.spring.domain.user.entity.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public class CustomOAuth2User implements OAuth2User, OidcUser {
    private final User user;
    private final Map<String, Object> attributes;
    private final String provider; // kakao, google

    public CustomOAuth2User(User user, Map<String, Object> attributes, String provider) {
        this.user = user;
        this.attributes = attributes;
        this.provider = provider;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return user.getSocialId(); // 또는 unique id
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public Map<String, Object> getClaims() {
        return attributes;
    }

    @Override
    public OidcIdToken getIdToken() {
        return null; // 필요시 userRequest.getIdToken() 전달, 우린 google에서 식별 가능한 값으로 sub를 받기 때문에 null 반환해도 정상적으로 동작함
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return null;
    }
}
