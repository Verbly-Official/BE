package verbly.spring.domain.payment.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import verbly.spring.domain.payment.service.PayPalService;
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

    // 1. Ready
    @PostMapping("/ready")
    public String ready(@RequestParam Long planId) {
        return payPalService.ready(planId);
    }

    // 2. Success
    @GetMapping("/success")
    public void success(
            @RequestParam("subscription_id") String subscriptionId,
            @RequestParam("planId") Long planId, // 아까 url 뒤에 붙여서 보낸거 받기
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

    // 3. Cancel
    @GetMapping("/cancel")
    public void cancel(HttpServletResponse response) throws IOException {
        response.sendRedirect(failUrl);
    }
}
