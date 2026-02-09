package verbly.spring.domain.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.payment.entity.SubscriptionPlan;

import java.util.List;

public interface PlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    List<SubscriptionPlan> findByIsActive(boolean b);
}
