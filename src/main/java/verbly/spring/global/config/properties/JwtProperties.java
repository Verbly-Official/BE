package verbly.spring.global.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties("jwt.token")
public class JwtProperties { // application.yml에 토큰 관련 값들을 불러와서 코드 상의 조작을 해야할 때, 해당 설정값들을 가져오는 방식 중 하나 --> JwtProperties 클래스를 생성
    private String secretKey="";
    private Expiration expiration;

    @Getter
    @Setter
    public static class Expiration{
        private Long access;
        private Long refresh;
    }
}
