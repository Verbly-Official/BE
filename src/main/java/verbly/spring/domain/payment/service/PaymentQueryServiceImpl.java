package verbly.spring.domain.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.payment.dto.PaymentResponseDTO;
import verbly.spring.domain.payment.entity.SubscriptionPlan;
import verbly.spring.domain.payment.repository.PlanRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryServiceImpl implements PaymentQueryService {
    private final PlanRepository planRepository;
    @Override
    public List<PaymentResponseDTO.PaymentPlan> getAllPaymentPlan() {
        List<SubscriptionPlan> subscriptionPlans = planRepository.findByIsActive(true);
        return subscriptionPlans.stream()
                .map(plan -> PaymentResponseDTO.PaymentPlan.builder()
                        .name(plan.getName())
                        .price(plan.getPrice())
                        .billingCycle(plan.getBillingCycle())
                        .build())
                .toList();
    }
}
