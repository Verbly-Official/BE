package verbly.spring.domain.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.payment.entity.Subscription;
import verbly.spring.domain.payment.entity.SubscriptionPlan;
import verbly.spring.domain.payment.enums.SubscriptionStatus;
import verbly.spring.domain.payment.repository.PlanRepository;
import verbly.spring.domain.payment.repository.SubscriptionRepository;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.repository.UserRepository;
import verbly.spring.infrastructure.kakao.KakaoPayClient;
import verbly.spring.infrastructure.kakao.dto.KakaoPayDTO;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionService {

    private final KakaoPayClient kakaoPayClient;
    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;

    public KakaoPayDTO.ReadyResponse ready(Long userId, Long planId, String orderId) {

        User user = userRepository.findById(userId).orElseThrow();
        SubscriptionPlan plan = planRepository.findById(planId).orElseThrow();

        return kakaoPayClient.ready(
                orderId,
                String.valueOf(userId),
                plan.getName(),
                1,
                plan.getPrice()
        );
    }

    public void approve(String pgToken, String tid, String orderId, String userIdStr, Long planId) {

        KakaoPayDTO.ApproveResponse response = kakaoPayClient.approve(tid, pgToken, orderId, userIdStr);

        Long userId = Long.parseLong(userIdStr);
        User user = userRepository.findById(userId).orElseThrow();
        SubscriptionPlan plan = planRepository.findById(planId).orElseThrow();

        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .sid(response.getSid())
                .status(SubscriptionStatus.ACTIVE)
                .lastPaymentDate(LocalDateTime.now())
                .nextPaymentDate(LocalDateTime.now().plusMonths(1))
                .build();

        subscriptionRepository.save(subscription);
    }
}