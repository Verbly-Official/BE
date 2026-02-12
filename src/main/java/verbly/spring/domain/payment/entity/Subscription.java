package verbly.spring.domain.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import verbly.spring.domain.payment.enums.BillingCycle;
import verbly.spring.domain.payment.enums.PaymentProvider;
import verbly.spring.domain.payment.enums.SubscriptionStatus;
import verbly.spring.domain.user.entity.User;
import verbly.spring.global.common.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Subscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private SubscriptionPlan plan;

    private String sid;

    @Enumerated(EnumType.STRING)
    private PaymentProvider provider;

    private LocalDateTime lastPaymentDate;

    private LocalDateTime nextPaymentDate;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    public void expire() {
        this.status = SubscriptionStatus.EXPIRED;
        this.nextPaymentDate = null;
    }

    public void renew() {
        this.lastPaymentDate = LocalDateTime.now();
        if (this.plan.getBillingCycle() == BillingCycle.MONTHLY) {
            this.nextPaymentDate = LocalDateTime.now().plusMonths(1);
        } else {
            this.nextPaymentDate = LocalDateTime.now().plusYears(1);
        }
    }
}
