package verbly.spring.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.payment.entity.Subscription;
import verbly.spring.domain.payment.enums.SubscriptionStatus;
import verbly.spring.domain.payment.repository.SubscriptionRepository;
import verbly.spring.infrastructure.kakao.KakaoPayClient;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final KakaoPayClient kakaoPayClient;

    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void autoPayment() {
        log.info("🔄 정기 결제 스케줄러 실행");
        List<Subscription> subscriptions = subscriptionRepository.findByNextPaymentDateBeforeAndStatus(
                LocalDateTime.now(),
                SubscriptionStatus.ACTIVE
        );

        for (Subscription sub : subscriptions) {
            try {
                kakaoPayClient.recurring(
                        sub.getSid(),

                        String.valueOf(sub.getUser().getId()),
                        sub.getPlan().getPrice()
                );

                sub.renew();
                log.info("✅ 결제 성공: user_id={}", sub.getUser().getId());

            } catch (Exception e) {
                log.error("❌ 결제 실패: user_id={}, cause={}", sub.getUser().getId(), e.getMessage());

            }
        }
    }
}
