package verbly.spring.global.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties("coolsms")
public class SmsProperties {
    private String apiKey;
    private String apiSecret;
    private String sender;
}
