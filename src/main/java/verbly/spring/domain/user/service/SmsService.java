package verbly.spring.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import verbly.spring.domain.user.dto.request.SmsRequestDTO;
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

    public void sendAuthCode(SmsRequestDTO.SendDTO request) {
        String authCode = smsUtil.generateAuthCode(); // 인증번호 생성
        String messageText = smsUtil.makeAuthMessage(authCode); // 메시지

        Message message = new Message();
        message.setFrom(smsProperties.getSender());
        message.setTo(request.getPhoneNumber());
        message.setText(messageText);

        SingleMessageSendingRequest requestSMS = new SingleMessageSendingRequest(message); // 요청 래핑

        // CoolSMS 발송 및 로그
        try {
            SingleMessageSentResponse response = messageService.sendOne(requestSMS);
            log.info("[SMS] 발송 성공 - to: {}", request.getPhoneNumber());
        } catch (Exception e) {
            log.error("[SMS] 발송 실패 - to: {}", request.getPhoneNumber(), e);
            throw new BaseException(ErrorStatus.SMS_SEND_FAILED);
        }

        // Redis에 인증번호 저장 (3분 유효)
        String redisKey = buildKey(request.getPhoneNumber());
        redisTemplate.opsForValue().set(redisKey, authCode, AUTH_CODE_TTL, TimeUnit.MINUTES);
    }

    public void verifyAuthCode(SmsRequestDTO.VerifyDTO request) {
        String redisKey = buildKey(request.getPhoneNumber());
        String storedCode = redisTemplate.opsForValue().get(redisKey);

        if (storedCode == null) {
            throw new BaseException(ErrorStatus.SMS_CODE_EXPIRED);
        }

        if (!storedCode.equals(request.getCode())) {
            throw new BaseException(ErrorStatus.SMS_CODE_NOT_MATCH);
        }

        redisTemplate.delete(redisKey); // 1회 검증 후 삭제

        String verifiedKey = buildVerifiedKey(request.getPhoneNumber());
        redisTemplate.opsForValue().set(verifiedKey, "true", AUTH_CODE_TTL, TimeUnit.MINUTES);
    }

    private String buildKey(String phoneNumber) {
        return "SMS:AUTH:PHONE:" + phoneNumber;
    }

    private String buildVerifiedKey(String phoneNumber) {
        return "SMS:VERIFIED:PHONE:" + phoneNumber;
    }
}
