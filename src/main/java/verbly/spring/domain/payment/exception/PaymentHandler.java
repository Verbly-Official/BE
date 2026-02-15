package verbly.spring.domain.payment.exception;

import verbly.spring.global.common.code.BaseErrorCode;
import verbly.spring.global.common.exception.BaseException;

public class PaymentHandler extends BaseException {
    public PaymentHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
