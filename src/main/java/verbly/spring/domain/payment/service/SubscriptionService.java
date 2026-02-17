package verbly.spring.domain.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.payment.entity.Subscription;
import verbly.spring.domain.payment.entity.SubscriptionPlan;
import verbly.spring.domain.payment.enums.BillingCycle;
import verbly.spring.domain.payment.enums.PaymentProvider;
import verbly.spring.domain.payment.enums.SubscriptionStatus;
import verbly.spring.domain.payment.exception.PaymentHandler;
import verbly.spring.domain.payment.repository.PlanRepository;
import verbly.spring.domain.payment.repository.SubscriptionRepository;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.repository.UserRepository;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.domain.payment.dto.KakaoPayDTO;
import verbly.spring.domain.payment.client.KakaoPayClient;

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
        SubscriptionPlan plan = planRepository.findById(planId).orElseThrow(()->new PaymentHandler(ErrorStatus.PAYMENTPLAN_NOT_FOUND));
        if(!plan.getIsActive()){
            throw new PaymentHandler(ErrorStatus.PAYMENTPLAN_NOT_ACTIVE);
        }
        return kakaoPayClient.ready(
                orderId,
                String.valueOf(userId),
                plan.getName(),
                1,
                plan.getPrice()
        );
    }

    /**
     * Approves a KakaoPay payment and creates an active Subscription for the corresponding user and plan.
     *
     * Calls the payment client to finalize approval, constructs a Subscription with the payment SID,
     * sets last and next payment dates based on the plan's billing cycle, marks the provider as KAKAO,
     * and persists the Subscription.
     *
     * @param pgToken    the payment gateway token returned by KakaoPay callback
     * @param tid        the transaction id assigned by KakaoPay
     * @param orderId    the merchant order identifier used for this payment
     * @param userIdStr  the user id as a string (will be parsed to Long)
     * @param planId     the id of the subscription plan to associate with the subscription
     */
    public void approve(String pgToken, String tid, String orderId, String userIdStr, Long planId) {

        KakaoPayDTO.ApproveResponse response = kakaoPayClient.approve(tid, pgToken, orderId, userIdStr);

        Long userId = Long.parseLong(userIdStr);
        User user = userRepository.findById(userId).orElseThrow();
        SubscriptionPlan plan = planRepository.findById(planId).orElseThrow();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextDate = calculateNextDate(now, plan.getBillingCycle());

        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .sid(response.getSid())
                .status(SubscriptionStatus.ACTIVE)
                .lastPaymentDate(now)
                .nextPaymentDate(nextDate)
                .provider(PaymentProvider.KAKAO)
                .build();

        subscriptionRepository.save(subscription);
    }

    private LocalDateTime calculateNextDate(LocalDateTime now, BillingCycle period) {
        if (period == BillingCycle.YEARLY) {
            return now.plusYears(1);
        }
        return now.plusMonths(1);
    }
}