package verbly.spring.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import verbly.spring.domain.user.service.SmsService;
import verbly.spring.global.common.code.SuccessStatus;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/phone")
public class SmsRestController {
    private final SmsService smsService;

    @PostMapping("/send")
    public ApiResponse<?> sendAuthCode(
            @RequestParam String phone
    ) {
        smsService.sendAuthCode(phone);
        return ApiResponse.onSuccess(SuccessStatus.SMS_SEND_COMPLETED);
    }

    @PostMapping("/verify")
    public ApiResponse<?> verifyAuthCode(
            @RequestParam String phone,
            @RequestParam String code
    ) {
        boolean result = smsService.verifyAuthCode(phone, code);

        if (result) {
            return ApiResponse.onSuccess(SuccessStatus.SMS_VERIFY_SUCCESS);
        }

        return ApiResponse.onFailure("SMS4001", "인증번호가 일치하지 않습니다.", null);
    }
}
