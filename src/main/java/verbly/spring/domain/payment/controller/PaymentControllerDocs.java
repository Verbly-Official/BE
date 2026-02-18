package verbly.spring.domain.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import verbly.spring.domain.payment.dto.PaymentResponseDTO;
import verbly.spring.domain.payment.dto.PaypalDTO;
import verbly.spring.global.common.response.ApiResponse;
import verbly.spring.global.infrastructure.kakao.dto.KakaoPayDTO;

import java.util.List;

@Tag(name = "Payment", description = "결제 관련 API")
public interface PaymentControllerDocs {

    @Operation(
            summary = "카카오페이 결제 준비 (Ready)",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = "사용자가 '구독하기' 버튼을 눌렀을 때 호출합니다. 카카오페이 결제 고유 번호(TID)를 발급받고, 결제 페이지 URL을 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                               "isSuccess": true,
                                               "code": "COMMON2000",
                                               "message": "성공입니다.",
                                               "result": {
                                                  "tid": "T98a3ab558d13d1da11e",
                                                  "next_redirect_pc_url": "https://online-payment.kakaopay.com/mockup/bridge/pc/pg/subscription/payment-issue/1b8084abca6f10c1ab3e0c733635e3d0c59eb0dcfcebe9675ed9596b0257c42b",
                                                  "next_redirect_mobile_url": "https://online-payment.kakaopay.com/mockup/bridge/mobile-web/pg/subscription/payment-issue/1b8084abca6f10c1ab3e0c733635e3d0c59eb0dcfcebe9675ed9596b0257c42b"
                                               }
                                            }
                                        """
                            )
                    )
            )
    })
    ApiResponse<KakaoPayDTO.ReadyResponse> ready(
            @Parameter(description = "구독할 플랜")
            @RequestParam Long planId,
            @Parameter(hidden = true) HttpSession session
    );

    @Operation(
            summary = "결제 플랜 리스트 반환",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = "결제 플랜 리스트 반환"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "isSuccess": true,
                                                "code": "COMMON2000",
                                                "message": "성공입니다.",
                                                "result": [
                                                    {
                                                       "name": "연간",
                                                       "price": 1000,
                                                       "billingCycle": "YEARLY"
                                                    }
                                                ]
                                            }
                                        """
                            )
                    )
            )
    })
    ApiResponse<List<PaymentResponseDTO.PaymentPlan>> paymentPlan();



    @Operation(
            summary = "페이팔 결제 준비 (Ready)",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = "사용자가 '구독하기' 버튼을 눌렀을 때 호출합니다. 결제 페이지 URL을 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "isSuccess": true,
                                                "code": "COMMON2000",
                                                "message": "성공입니다.",
                                                "result": "https://www.sandbox.paypal.com/webapps/billing/subscriptions?ba_token=BA-9B357889L1492641Y"
                                            }
                                        """
                            )
                    )
            )
    })
    ApiResponse<String> ready(Long planId);

    @Operation(
            summary = "페이팔 결제 최종 승인 (Complete)",
            description = "페이팔 결제 창에서 승인 후, 리다이렉트된 페이지에서 받은 정보를 서버로 전송합니다." +
                    "String subscriptionId: 페이팔 구독 ID (I-로 시작하는 문자열)" +
                    "Long planId: 사용자가 구매한 플랜 ID",
            security = @SecurityRequirement(name = "JWT TOKEN")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                               "isSuccess": true,
                                               "code": "COMMON2000",
                                               "message": "성공입니다.",
                                               "result": "구독이 성공적으로 완료되었습니다."
                                            }
                                        """
                            )
                    )
            )
    })
    ApiResponse<String> complete(@RequestBody PaypalDTO.PaymentCompleteRequestDto request);
}
