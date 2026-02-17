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
    private final PlanRepository planRepository;
    private final UserRepository userRepository;

    @Value("${paypal.plan-id.monthly}") private String monthlyPlanId;
    @Value("${paypal.plan-id.yearly}") private String yearlyPlanId;
    @Value("${paypal.full-urls.backend.success}") private String successBackend;
    @Value("${paypal.full-urls.backend.fail}") private String cancelBackend;

    /**
     * Prepares a PayPal subscription for the specified internal plan and returns the PayPal creation URL.
     *
     * @param planId internal identifier of the subscription plan to subscribe to
     * @return the PayPal subscription creation URL or token returned by the PayPal client
     * @throws PaymentHandler if the plan does not exist or is not active
     */
    public String ready(Long planId) {

        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId).orElseThrow(()-> new PaymentHandler(ErrorStatus.PAYMENTPLAN_NOT_FOUND));
        if(!plan.getIsActive()){
            throw new PaymentHandler(ErrorStatus.PAYMENTPLAN_NOT_ACTIVE);
        }
        String realPayPalPlanId = (plan.getBillingCycle() == BillingCycle.MONTHLY) ? monthlyPlanId : yearlyPlanId;

        String returnUrl = successBackend + planId;

        return paypalClient.createSubscription(realPayPalPlanId, returnUrl, cancelBackend);
    }

    /**
     * Finalizes and persists a PayPal subscription after PayPal reports success.
     *
     * Retrieves PayPal subscription status, validates it, loads the user and subscription plan,
     * creates a Subscription with provider set to PAYPAL and status set to ACTIVE, sets
     * lastPaymentDate to now and nextPaymentDate according to the plan's billing cycle, and
     * persists the Subscription.
     *
     * @param subscriptionId the PayPal subscription identifier
     * @param userId         the internal user identifier to associate with the subscription
     * @param planId         the internal subscription plan identifier to apply
     * @throws PaymentHandler if the PayPal subscription status is not acceptable or the plan is not found
     * @throws UserHandler    if the user is not found
     */
    @Transactional
    public void success(String subscriptionId, Long userId, Long planId) {
        PaypalDTO.SubscriptionResponse info = paypalClient.getSubscriptionStatus(subscriptionId);
        if (!"ACTIVE".equals(info.getStatus()) && !"APPROVAL_PENDING".equals(info.getStatus())) {
            throw new PaymentHandler(ErrorStatus.PAYMENTPLAN_NOT_FOUND);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new PaymentHandler(ErrorStatus.PAYMENTPLAN_NOT_FOUND));

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
        log.info("✅ 페이팔 구독 저장 완료: User {}, SID {}", userId, subscriptionId);
    }
}