package verbly.spring.domain.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import verbly.spring.domain.payment.enums.BillingCycle;
import verbly.spring.global.common.entity.BaseEntity;

@Entity
@Getter
public class SubscriptionPlan extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int price;

    @Enumerated(EnumType.STRING)
    private BillingCycle billingCycle; // MONTHLY, YEARLY

    Boolean isActive;
}
