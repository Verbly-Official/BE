package verbly.spring.domain.payment.controller;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import verbly.spring.domain.payment.service.PayPalService;
import verbly.spring.global.common.response.ApiResponse;
import verbly.spring.global.security.utils.SecurityUtils;

import java.io.IOException;

@RestController
@RequestMapping("/api/payment/paypal")
@RequiredArgsConstructor
public class PayPalController {

    private final PayPalService payPalService;

    @Value("${paypal.full-urls.frontend.success}")
    private String successUrl;

    @Value("${paypal.full-urls.frontend.fail}")
    private String failUrl;

    @PostMapping("/ready")
    public ApiResponse<String> ready(@RequestParam Long planId) {
        return ApiResponse.onSuccess(payPalService.ready(planId));
    }

    @Hidden
    @GetMapping("/success")
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

    @Hidden
    @GetMapping("/cancel")
    public void cancel(HttpServletResponse response) throws IOException {
        response.sendRedirect(failUrl);
    }
}
