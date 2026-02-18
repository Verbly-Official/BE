package verbly.spring.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import verbly.spring.global.infrastructure.paypal.PaypalClient;
import verbly.spring.global.infrastructure.paypal.dto.PaypalDTO;
import verbly.spring.domain.payment.entity.Subscription;
import verbly.spring.domain.payment.entity.SubscriptionPlan;
import verbly.spring.domain.payment.enums.BillingCycle;
import verbly.spring.domain.payment.enums.PaymentProvider;
import verbly.spring.domain.payment.enums.SubscriptionStatus;
import verbly.spring.domain.payment.exception.PaymentHandler;
import verbly.spring.domain.payment.repository.PlanRepository;
import verbly.spring.domain.payment.repository.SubscriptionRepository;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.exception.UserHandler;
import verbly.spring.domain.user.repository.UserRepository;
import verbly.spring.global.common.code.ErrorStatus;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayPalService {

    private final PaypalClient paypalClient;
    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository subscriptionPlanRepository;
    private final UserRepository userRepository;

    // application.yml의 페이팔 플랜 ID
    @Value("${paypal.plan-id.monthly}")
    private String monthlyPlanId;

    @Value("${paypal.plan-id.yearly}")
    private String yearlyPlanId;

    // ✅ [변경] 프론트엔드 성공 URL (카카오와 공유)
    // 값: https://www.verbly.kr/my/korean
    @Value("${kakao.pay.redirect-url.success-f}")
    private String frontendSuccessUrl;

    // ✅ [변경] 프론트엔드 실패 URL (카카오와 공유)
    // 값: https://www.verbly.kr/my/korean/payment
    @Value("${kakao.pay.redirect-url.fail-f}")
    private String frontendFailUrl;

    /**
     * 1. 결제 준비: 페이팔 승인 URL 생성
     * 페이팔에게 "결제 끝나면 지정된 프론트엔드 페이지로 보내줘"라고 요청함
     */
    public String ready(Long planId) {
        // 1. 플랜 조회 및 유효성 검사
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new PaymentHandler(ErrorStatus.PAYMENTPLAN_NOT_FOUND));

        if (!plan.getIsActive()) {
            throw new PaymentHandler(ErrorStatus.PAYMENTPLAN_NOT_ACTIVE);
        }

        // 2. 실제 페이팔 플랜 ID 결정 (월간 vs 연간)
        String realPayPalPlanId = (plan.getBillingCycle() == BillingCycle.MONTHLY) ? monthlyPlanId : yearlyPlanId;

        // 3. ✅ [핵심] 리턴 URL 설정
        // frontendSuccessUrl 뒤에 planId를 붙여서 보냅니다.
        // 페이팔이 리다이렉트할 때 subscription_id 등을 자동으로 더 붙여줍니다.
        // 최종 형태 예시: https://www.verbly.kr/my/korean?planId=1&subscription_id=I-XXX...
        String returnUrl = UriComponentsBuilder.fromUriString(frontendSuccessUrl)
                .queryParam("planId", planId)  // 프론트가 알 수 있게 planId 전달
                .build()
                .toUriString();

        // 4. 취소 URL 설정 (실패 시 이동할 곳)
        String cancelUrl = frontendFailUrl;

        // 5. 페이팔 클라이언트 호출하여 승인 링크(approval_url) 반환
        return paypalClient.createSubscription(realPayPalPlanId, returnUrl, cancelUrl);
    }


    /**
     * 2. 결제 최종 승인
     * 프론트엔드가 페이팔 승인 후 백엔드로 호출하는 메서드 (JWT 인증 상태)
     */
    @Transactional
    public void success(String subscriptionId, Long userId, Long planId) {
        if (subscriptionRepository.existsBySid(subscriptionId)) {
            log.info("⚠️ 이미 처리된 구독 건입니다. SID: {}", subscriptionId);
            return;
        }

        // 2. 페이팔 API를 호출하여 실제 상태 확인 (교차 검증)
        PaypalDTO.SubscriptionResponse info = paypalClient.getSubscriptionStatus(subscriptionId);

        // 페이팔 상에서 ACTIVE 상태가 아니면 승인 불가
        if (!"ACTIVE".equals(info.getStatus())) {
            log.error("❌ 결제 승인 실패 - 페이팔 상태가 ACTIVE가 아님: {}", info.getStatus());
            throw new PaymentHandler(ErrorStatus.PAYPAL_SUBSCRIPTION_ERROR);
        }

        // 3. 유저 및 플랜 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new PaymentHandler(ErrorStatus.PAYMENTPLAN_NOT_FOUND));

        String dbPayPalId = plan.getPaypalPlanId();
        String actualPayPalId = info.getPlanId();

        if (dbPayPalId == null || !dbPayPalId.equals(actualPayPalId)) {
            log.warn("🚨 플랜 변조 감지! User: {}, DB_Plan: {}, Actual_PayPal_Plan: {}",
                    userId, dbPayPalId, actualPayPalId);
            throw new PaymentHandler(ErrorStatus.PAYPAL_PLAN_MISMATCH);
        }

        // 4. ✅ [보안] 플랜 변조 방지
        // 사용자가 요청한 planId에 해당하는 실제 페이팔 ID가, 페이팔이 응답한 ID와 같은지 확인
        String expectedPaypalPlanId = (plan.getBillingCycle() == BillingCycle.MONTHLY) ? monthlyPlanId : yearlyPlanId;

        if (!expectedPaypalPlanId.equals(info.getPlanId())) {
            log.warn("🚨 플랜 변조 시도 감지! User: {}, 요청Plan: {}, 실제페이팔Plan: {}",
                    userId, expectedPaypalPlanId, info.getPlanId());
            throw new PaymentHandler(ErrorStatus.PAYPAL_PLAN_MISMATCH);
        }

        // 5. 구독 정보 저장
        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .sid(subscriptionId)
                .provider(PaymentProvider.PAYPAL)
                .status(SubscriptionStatus.ACTIVE)
                .lastPaymentDate(LocalDateTime.now())
                .nextPaymentDate(LocalDateTime.now().plusMonths(plan.getBillingCycle() == BillingCycle.MONTHLY ? 1 : 12))
                .build();

        subscriptionRepository.save(subscription);
        log.info("✅ 페이팔 구독 최종 승인 및 저장 완료: User {}, SID {}", userId, subscriptionId);
    }

    public void syncSingleSubscription(Subscription sub) {
        try {
            PaypalDTO.SubscriptionResponse info = paypalClient.getSubscriptionStatus(sub.getSid());
            updateSubscriptionInDb(sub, info);
        } catch (Exception e) {
            log.error("구독 갱신 실패 SID: {}", sub.getSid(), e);
        }
    }

    @Transactional
    public void updateSubscriptionInDb(Subscription sub, PaypalDTO.SubscriptionResponse info) {
        if ("ACTIVE".equals(info.getStatus())) {
            sub.renew();
        } else {
            sub.expire();
            log.info("❌ 구독 만료 처리됨: SID {}", sub.getSid());
        }
        subscriptionRepository.save(sub);
    }
}