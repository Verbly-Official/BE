package verbly.spring.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import verbly.spring.global.config.properties.SmsProperties;

@RequiredArgsConstructor
@Configuration
public class SmsConfig {
    private final SmsProperties smsProperties;

    @Bean
    public DefaultMessageService messageService() {
        return new DefaultMessageService(smsProperties.getApiKey(), smsProperties.getApiSecret(), "https://api.coolsms.co.kr");
    }
}
