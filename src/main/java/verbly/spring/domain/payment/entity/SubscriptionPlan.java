package verbly.spring.domain.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import verbly.spring.domain.payment.enums.BillingCycle;
import verbly.spring.global.common.entity.BaseEntity;

import java.math.BigDecimal;

@Entity
@Getter
public class SubscriptionPlan extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    private String paypalPlanId;

    @Enumerated(EnumType.STRING)
    private BillingCycle billingCycle;

    Boolean isActive;
}