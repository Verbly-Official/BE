package verbly.spring.domain.review.exception;

import verbly.spring.global.common.code.BaseErrorCode;
import verbly.spring.global.common.exception.BaseException;

public class ReviewHandler extends BaseException {
    public ReviewHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
