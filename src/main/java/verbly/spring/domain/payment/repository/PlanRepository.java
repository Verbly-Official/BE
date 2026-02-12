package verbly.spring.domain.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.payment.entity.SubscriptionPlan;

public interface PlanRepository extends JpaRepository<SubscriptionPlan, Long> {
}
