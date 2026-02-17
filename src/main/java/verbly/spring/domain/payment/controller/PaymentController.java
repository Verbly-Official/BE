package verbly.spring.domain.payment.controller;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
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
public class PaymentController implements PaymentControllerDocs {

    private final SubscriptionService subscriptionService;
    private final PaymentQueryService paymentQueryService;
    private final PayPalService payPalService;

    @Value("${pay.kakao.full-urls.frontend.success}")
    private String successUrl;

    @Value("${pay.kakao.full-urls.frontend.fail}")
    private String failUrl;

    /**
     * Prepare a Kakao Pay payment for the given plan and persist readiness data in the HTTP session.
     *
     * @param planId the identifier of the payment plan to prepare
     * @param session the HTTP session where `tid`, `planId`, `userId`, and `orderId` will be stored
     * @return an ApiResponse wrapping the KakaoPayDTO.ReadyResponse containing Kakao Pay readiness details
     */
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

    /**
     * Handle Kakao Pay success callback and finalize the subscription approval.
     *
     * @param pgToken the `pg_token` returned by Kakao Pay used to approve the payment
     * @param session the HTTP session containing `tid`, `userId`, `planId`, and `orderId` from the readiness step
     * @param response used to redirect the client to the configured success URL
     * @throws IOException if sending the redirect fails
     */
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

    /**
     * Redirects the client to the configured payment failure URL.
     *
     * @throws IOException if sending the redirect fails
     */
    @Hidden
    @GetMapping("/kakao/cancel")
    public void kakaoCancel(HttpServletResponse response) throws IOException {
        response.sendRedirect(failUrl);
    }

    /**
     * Handle Kakao Pay failure callback by redirecting the client to the configured failure URL.
     *
     * @param response the HTTP response used to perform the redirect
     * @throws IOException if sending the redirect fails
     */
    @Hidden
    @GetMapping("/kakao/fail")
    public void fail(HttpServletResponse response) throws IOException {
        response.sendRedirect(failUrl);
    }

    /**
     * Retrieve all available payment plans.
     *
     * @return an ApiResponse containing a list of available PaymentPlan DTOs
     */
    @Override
    @GetMapping("/plan")
    public ApiResponse<List<PaymentResponseDTO.PaymentPlan>> paymentPlan(){
        return ApiResponse.onSuccess(paymentQueryService.getAllPaymentPlan());
    }

    /**
     * Initiates a PayPal checkout for the given payment plan and returns the approval URL.
     *
     * @param planId the identifier of the payment plan to create a PayPal checkout for
     * @return the PayPal approval URL to which the client should be redirected
     */
    @Override
    @PostMapping("/paypal/ready")
    public ApiResponse<String> ready(@RequestParam Long planId) {
        return ApiResponse.onSuccess(payPalService.ready(planId));
    }

    /**
     * Handle PayPal subscription success callback and redirect the client to the configured frontend URL.
     *
     * Calls the service to finalize the subscription; on successful processing redirects to the configured success URL,
     * and on error redirects to the configured failure URL.
     *
     * @param subscriptionId the PayPal subscription identifier returned by PayPal
     * @param planId the application plan identifier associated with the subscription
     * @param response the HTTP response used to perform the redirect
     * @throws IOException if sending the redirect fails
     */
    @Hidden
    @GetMapping("/paypal/success")
    public void success(
            @RequestParam("subscription_id") String subscriptionId,
            @RequestParam("planId") Long planId,
            HttpServletResponse response
    ) throws IOException {

        Long userId = SecurityUtils.getCurrentUserId();

        try {
            payPalService.success(subscriptionId, userId, planId);
            response.sendRedirect(successUrl);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(failUrl);
        }
    }

    /**
     * Redirects the client to the configured payment failure URL.
     *
     * @param response the HTTP response used to perform the redirect
     * @throws IOException if sending the redirect fails
     */
    @Hidden
    @GetMapping("/paypal/cancel")
    public void paypalCancel(HttpServletResponse response) throws IOException {
        response.sendRedirect(failUrl);
    }
}