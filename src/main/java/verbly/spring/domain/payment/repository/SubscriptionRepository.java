package verbly.spring.domain.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.payment.entity.Subscription;
import verbly.spring.domain.payment.enums.PaymentProvider;
import verbly.spring.domain.payment.enums.SubscriptionStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByProviderAndStatus(PaymentProvider provider, SubscriptionStatus status);
    List<Subscription> findByNextPaymentDateBeforeAndStatus(
            LocalDateTime now,
            SubscriptionStatus status
    );

    boolean existsBySid(String sid);
}
