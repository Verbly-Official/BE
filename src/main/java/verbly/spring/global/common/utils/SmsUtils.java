package verbly.spring.global.common.utils;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class SmsUtils {
    public String generateAuthCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    public String makeAuthMessage(String authCode) {
        return "[Verbly 인증번호] " + authCode +
                "\n본인 확인을 위해 인증번호를 입력해주세요.";
    }
}
