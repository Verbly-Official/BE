package verbly.spring.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.payment.client.PaypalClient;
import verbly.spring.domain.payment.dto.PaypalDTO;
import verbly.spring.domain.payment.entity.Subscription;
import verbly.spring.domain.payment.enums.PaymentProvider;
import verbly.spring.domain.payment.enums.SubscriptionStatus;
import verbly.spring.domain.payment.repository.SubscriptionRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PayPalScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final PaypalClient paypalClient;

    /**
     * Synchronizes local PayPal subscriptions with their current PayPal statuses.
     *
     * <p>Finds subscriptions with provider PAYPAL and status ACTIVE, queries PayPal for each subscription's
     * current status, renews subscriptions that are ACTIVE on PayPal, and expires others. Errors while
     * processing an individual subscription are logged and do not interrupt processing of the remaining subscriptions.
     *
     * <p>This method is scheduled to run daily at 04:00 and executes within a transactional context.
     */
    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void checkPayPalStatus() {
        log.info("🔄 페이팔 상태 동기화 시작");

        List<Subscription> subs = subscriptionRepository.findByProviderAndStatus(
                PaymentProvider.PAYPAL,
                SubscriptionStatus.ACTIVE
        );

        for (Subscription sub : subs) {
            try {
                PaypalDTO.SubscriptionResponse info = paypalClient.getSubscriptionStatus(sub.getSid());

                if ("ACTIVE".equals(info.getStatus())) {
                    sub.renew();
                } else {
                    sub.expire();
                    log.info("❌ 구독 만료 처리됨: SID {}", sub.getSid());
                }
            } catch (Exception e) {
                log.error("스케줄러 에러 (SID: {}): {}", sub.getSid(), e.getMessage());
            }
        }
    }
}