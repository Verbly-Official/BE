package verbly.spring.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import verbly.spring.global.common.utils.SmsUtils;
import verbly.spring.global.config.properties.SmsProperties;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {
    private final DefaultMessageService messageService;
    private final SmsUtils smsUtil;
    private final StringRedisTemplate redisTemplate;
    private final SmsProperties smsProperties;

    private static final long AUTH_CODE_TTL = 3;

    public void sendAuthCode(String phoneNumber) {
        String authCode = smsUtil.generateAuthCode(); // 인증번호 생성
        String messageText = smsUtil.makeAuthMessage(authCode); // 메시지

        Message message = new Message();
        message.setFrom(smsProperties.getSender());
        message.setTo(phoneNumber);
        message.setText(messageText);

        SingleMessageSendingRequest request = new SingleMessageSendingRequest(message); // 요청 래핑

        // CoolSMS 발송 및 로그
        try {
            SingleMessageSentResponse response = messageService.sendOne(request);
            log.info("[SMS] 발송 성공 - to: {}", phoneNumber);
        } catch (Exception e) {
            log.error("[SMS] 발송 실패 - to: {}", phoneNumber, e);
            throw new RuntimeException("SMS 발송에 실패했습니다.");
        }

        // Redis에 인증번호 저장 (3분 유효)
        String redisKey = "SMS:AUTH:PHONE:" + phoneNumber;
        redisTemplate.opsForValue().set(redisKey, authCode, AUTH_CODE_TTL, TimeUnit.MINUTES);
    }

    public boolean verifyAuthCode(String phoneNumber, String inputCode) {

        String redisKey = "SMS:AUTH:PHONE:" + phoneNumber;
        String storedCode = redisTemplate.opsForValue().get(redisKey);

        if (storedCode != null && storedCode.equals(inputCode)) {
            redisTemplate.delete(redisKey); // 1회 검증 후 삭제
            return true;
        }

        return false;
    }
}
