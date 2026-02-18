package verbly.spring.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
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
    private final PayPalService payPalService; // 서비스 주입

    @Scheduled(cron = "0 0 4 * * *")
    public void checkPayPalStatus() {
        log.info("🔄 페이팔 상태 동기화 시작");

        // 1. 활성화된 구독 목록 가져오기 (단순 조회라 트랜잭션 짧게 끝남)
        List<Subscription> subs = subscriptionRepository.findByProviderAndStatus(
                PaymentProvider.PAYPAL,
                SubscriptionStatus.ACTIVE
        );

        // 2. 하나씩 서비스로 넘기기
        for (Subscription sub : subs) {
            // 여기서 에러가 터져도 다음 사람(for문)은 계속 돌아갑니다.
            payPalService.syncSingleSubscription(sub);
        }

        log.info("✅ 동기화 작업 종료. 총 {}건 처리 시도.", subs.size());
    }
}
