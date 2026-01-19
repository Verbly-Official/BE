package verbly.spring.global.security.auth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import verbly.spring.domain.user.entity.User;

import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomUserDetails implements UserDetails {
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public Long getUserId() {
        return user.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 예시: "ROLE_USER" 권한을 부여
//        return Collections.singleton(() -> "ROLE_USER"); // () -> "ROLE_USER" 대신 아래처럼 바꾸면 IDE 지원, 오타 방지, 디버깅에 유리
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        // 소셜 로그인 기반이라면 비밀번호는 없음
        return "";
    }

    @Override
    public String getUsername() {
        // 여기서는 socialId를 식별자로 사용
        return user.getSocialId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
