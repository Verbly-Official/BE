package verbly.spring.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import verbly.spring.domain.payment.enums.BillingCycle;
import verbly.spring.domain.post.enums.PostStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PaymentResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentPlan{
        private Long planId;
        private String name;
        private Double price;
        BillingCycle billingCycle;
    }
}
