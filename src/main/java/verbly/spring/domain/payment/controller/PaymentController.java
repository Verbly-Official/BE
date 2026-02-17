package verbly.spring.domain.payment.controller;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import verbly.spring.domain.payment.dto.PaymentResponseDTO;
import verbly.spring.domain.payment.service.PayPalService;
import verbly.spring.domain.payment.service.PaymentQueryService;
import verbly.spring.domain.payment.service.SubscriptionService;
import verbly.spring.global.common.response.ApiResponse;
import verbly.spring.global.security.utils.SecurityUtils;
import verbly.spring.domain.payment.dto.KakaoPayDTO;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController implements PaymentControllerDocs {

    private final SubscriptionService subscriptionService;
    private final PaymentQueryService paymentQueryService;
    private final PayPalService payPalService;

    @Value("${pay.kakao.full-urls.frontend.success}")
    private String successUrl;

    @Value("${pay.kakao.full-urls.frontend.fail}")
    private String failUrl;

    @Override
    @PostMapping("/kakao/ready")
    public ApiResponse<KakaoPayDTO.ReadyResponse> ready(@RequestParam Long planId, HttpSession session) {
        Long userId = SecurityUtils.getCurrentUserId();
        String orderId = "order_" + userId + "_" + System.currentTimeMillis();

        KakaoPayDTO.ReadyResponse response = subscriptionService.ready(userId, planId, orderId);

        session.setAttribute("tid", response.getTid());
        session.setAttribute("planId", planId);
        session.setAttribute("userId", String.valueOf(userId));
        session.setAttribute("orderId", orderId);

        return ApiResponse.onSuccess(response);
    }

    @Hidden
    @GetMapping("/kakao/success")
    public void success(
            @RequestParam("pg_token") String pgToken,
            HttpSession session,
            HttpServletResponse response
    ) throws IOException {

        String tid = (String) session.getAttribute("tid");
        String userId = (String) session.getAttribute("userId");
        Long planId = (Long) session.getAttribute("planId");
        String orderId = (String) session.getAttribute("orderId");

        subscriptionService.approve(pgToken, tid, orderId, userId, planId);

        response.sendRedirect(successUrl);
    }

    @Hidden
    @GetMapping("/kakao/cancel")
    public void kakaoCancel(HttpServletResponse response) throws IOException {
        response.sendRedirect(failUrl);
    }

    @Hidden
    @GetMapping("/kakao/fail")
    public void fail(HttpServletResponse response) throws IOException {
        response.sendRedirect(failUrl);
    }

    @Override
    @GetMapping("/plan")
    public ApiResponse<List<PaymentResponseDTO.PaymentPlan>> paymentPlan(){
        return ApiResponse.onSuccess(paymentQueryService.getAllPaymentPlan());
    }

    @Override
    @PostMapping("/paypal/ready")
    public ApiResponse<String> ready(@RequestParam Long planId) {
        return ApiResponse.onSuccess(payPalService.ready(planId));
    }

    @Hidden
    @GetMapping("/paypal/success/{planId}")
    public void success(
            @RequestParam("subscription_id") String subscriptionId,
            @PathVariable("planId") Long planId,

            HttpServletResponse response
    ) throws IOException {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            payPalService.success(subscriptionId, userId, planId);
            response.sendRedirect(successUrl);
        } catch (Exception e) {
            log.error("PayPal 결제 성공 처리 중 에러 발생 - SubID: {}, UserId: {}", subscriptionId, e.getMessage(), e);
            response.sendRedirect(failUrl);
        }
    }

    @Hidden
    @GetMapping("/paypal/cancel")
    public void paypalCancel(HttpServletResponse response) throws IOException {
        response.sendRedirect(failUrl);
    }
}
