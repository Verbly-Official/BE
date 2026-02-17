package verbly.spring.domain.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.payment.entity.Subscription;
import verbly.spring.domain.payment.enums.PaymentProvider;
import verbly.spring.domain.payment.enums.SubscriptionStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    /**
 * Finds subscriptions for a given payment provider and subscription status.
 *
 * @param provider the payment provider to match
 * @param status   the subscription status to match
 * @return         a list of subscriptions that match the given provider and status; empty if none found
 */
List<Subscription> findByProviderAndStatus(PaymentProvider provider, SubscriptionStatus status);
    /**
     * Find subscriptions whose next payment date is before the given cutoff and that have the specified status.
     *
     * @param now    the cutoff LocalDateTime; subscriptions with nextPaymentDate before this value are returned
     * @param status the subscription status to match
     * @return       a list of subscriptions with nextPaymentDate before `now` and matching `status`
     */
    List<Subscription> findByNextPaymentDateBeforeAndStatus(
            LocalDateTime now,
            SubscriptionStatus status
    );
}