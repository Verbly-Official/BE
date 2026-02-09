package verbly.spring.domain.payment.controller;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import verbly.spring.domain.payment.service.SubscriptionService;
import verbly.spring.global.security.utils.SecurityUtils;
import verbly.spring.infrastructure.kakao.dto.KakaoPayDTO;

import java.io.IOException;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/ready")
    public KakaoPayDTO.ReadyResponse ready(@RequestParam Long planId, HttpSession session) {
        Long userId = SecurityUtils.getCurrentUserId();
        String orderId = "order_" + userId + "_" + System.currentTimeMillis();

        KakaoPayDTO.ReadyResponse response = subscriptionService.ready(userId, planId, orderId);

        session.setAttribute("tid", response.getTid());
        session.setAttribute("planId", planId);
        session.setAttribute("userId", String.valueOf(userId));
        session.setAttribute("orderId", orderId);

        return response;
    }

    @Hidden
    @GetMapping("/success")
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

        response.sendRedirect("http://localhost:3000/mypage/subscription?status=success");
        //https://www.verbly.kr/mypage/subscription?status=success ?
    }

    @Hidden
    @GetMapping("/cancel")
    public void cancel(HttpServletResponse response) throws IOException {
        response.sendRedirect("http://localhost:3000/mypage/subscription?status=cancel");
        //https://www.verbly.kr/mypage/subscription?status=cancel ?
    }

    @Hidden
    @GetMapping("/fail")
    public void fail(HttpServletResponse response) throws IOException {
        response.sendRedirect("http://localhost:3000/mypage/subscription?status=fail");
        //https://www.verbly.kr/mypage/subscription?status=fail ?
    }
}
