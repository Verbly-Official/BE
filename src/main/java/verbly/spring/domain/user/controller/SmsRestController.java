package verbly.spring.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import verbly.spring.domain.user.service.SmsService;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.common.code.SuccessStatus;

@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "SMS", description = "회원 전화번호 인증 관련 API")
@RequestMapping("/api/user/phone")
public class SmsRestController {
    private final SmsService smsService;

    @PostMapping("/send")
    @Operation(
            summary = "회원 전화번호 수정 시 인증번호 발송 API - JWT AccessToken 인증 필요",
            description = "JWT 인증된 유저가 입력한 전화번호로 인증번호를 발송합니다. 발송된 인증번호는 3분간 유효합니다.",
            security = @SecurityRequirement(name = "JWT TOKEN")
    )
    public ApiResponse<?> sendAuthCode(
            @Valid @RequestBody SmsRequestDTO.SendDTO request
    ) {
        smsService.sendAuthCode(request);
        return ApiResponse.onSuccess(SuccessStatus.SMS_SEND_COMPLETED);
    }

    @PostMapping("/verify")
    @Operation(
            summary = "전화번호 인증번호 검증 API",
            description = "사용자가 입력한 인증번호를 검증합니다. 인증 성공 시 해당 인증번호는 즉시 만료됩니다.",
            security = @SecurityRequirement(name = "JWT TOKEN")
    )
    public ApiResponse<?> verifyAuthCode(
            @Valid @RequestBody SmsRequestDTO.VerifyDTO request
    ) {
        smsService.verifyAuthCode(request);
        return ApiResponse.onSuccess(SuccessStatus.SMS_VERIFY_SUCCESS);
    }
}
