package verbly.spring.domain.payment.service;

import verbly.spring.domain.payment.dto.PaymentResponseDTO;

import java.util.List;

public interface PaymentQueryService {
    List<PaymentResponseDTO.PaymentPlan> getAllPaymentPlan();
}
