package verbly.spring.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.payment.client.PaypalClient;
import verbly.spring.domain.payment.dto.PaypalDTO;
import verbly.spring.domain.payment.entity.Subscription;
import verbly.spring.domain.payment.entity.SubscriptionPlan;
import verbly.spring.domain.payment.enums.PaymentProvider;
import verbly.spring.domain.payment.enums.SubscriptionStatus;
import verbly.spring.domain.payment.repository.PlanRepository;
import verbly.spring.domain.payment.repository.SubscriptionRepository;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.repository.UserRepository;


import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayPalService {

    private final PaypalClient paypalClient;
    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;

    @Value("${paypal.plan-id.monthly}") private String monthlyPlanId;
    @Value("${paypal.plan-id.yearly}") private String yearlyPlanId;
    @Value("${paypal.full-urls.backend.success}") private String successBackend;
    @Value("${paypal.full-urls.backend.fail}") private String cancelBackend;

    // [Ready] 결제창 URL 생성
    public String ready(Long planId) {
        // DB에 있는 Plan ID(1, 2)를 받아서 -> 페이팔용 Plan ID(P-...)로 변환
        String realPayPalPlanId = (planId == 1L) ? monthlyPlanId : yearlyPlanId;

        String returnUrl = successBackend + planId; // planId를 달아서 보냄 (나중에 저장할 때 필요)

        return paypalClient.createSubscription(realPayPalPlanId, returnUrl, cancelBackend);
    }

    // [Success] 승인 확인 및 엔티티 저장
    @Transactional
    public void success(String subscriptionId, Long userId, Long planId) {
        // 1. 페이팔 상태 검증
        PaypalDTO.SubscriptionResponse info = paypalClient.getSubscriptionStatus(subscriptionId);

        if (!"ACTIVE".equals(info.getStatus()) && !"APPROVAL_PENDING".equals(info.getStatus())) {
            throw new RuntimeException("구독이 유효하지 않습니다.");
        }

        // 2. 엔티티 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("플랜 없음"));

        // 3. Subscription 엔티티 생성 및 저장
        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .sid(subscriptionId)          // ⭐️ 페이팔 ID (I-XXXX) 저장
                .provider(PaymentProvider.PAYPAL) // ⭐️ 페이팔로 설정
                .status(SubscriptionStatus.ACTIVE)
                .lastPaymentDate(LocalDateTime.now())
                // 초기 다음 결제일 설정 (엔티티 내부 로직 사용해도 됨)
                .nextPaymentDate(LocalDateTime.now().plusMonths(plan.getBillingCycle() == verbly.spring.domain.payment.enums.BillingCycle.MONTHLY ? 1 : 12))
                .build();

        subscriptionRepository.save(subscription);
        log.info("✅ 페이팔 구독 저장 완료: User {}, SID {}", userId, subscriptionId);
    }
}
