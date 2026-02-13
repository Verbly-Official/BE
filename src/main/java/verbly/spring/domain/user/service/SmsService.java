package verbly.spring.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.common.exception.BaseException;
import verbly.spring.global.common.utils.SmsUtils;
import verbly.spring.global.config.properties.SmsProperties;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {
    private final DefaultMessageService messageService;
    private final SmsUtils smsUtil;
    private final StringRedisTemplate redisTemplate;
    private final SmsProperties smsProperties;

    private static final long AUTH_CODE_TTL = 3; // 3분

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
            throw new BaseException(ErrorStatus.SMS_SEND_FAILED);
        }

        // Redis에 인증번호 저장 (3분 유효)
        String redisKey = buildKey(phoneNumber);
        redisTemplate.opsForValue().set(redisKey, authCode, AUTH_CODE_TTL, TimeUnit.MINUTES);
    }

    public void verifyAuthCode(String phoneNumber, String inputCode) {
        String redisKey = buildKey(phoneNumber);
        String storedCode = redisTemplate.opsForValue().get(redisKey);

        if (storedCode == null) {
            throw new BaseException(ErrorStatus.SMS_CODE_EXPIRED);
        }

        if (!storedCode.equals(inputCode)) {
            throw new BaseException(ErrorStatus.SMS_CODE_NOT_MATCH);
        }

        redisTemplate.delete(redisKey); // 1회 검증 후 삭제
    }

    private String buildKey(String phoneNumber) {
        return "SMS:AUTH:PHONE:" + phoneNumber;
    }
}
