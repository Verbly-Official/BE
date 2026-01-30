package verbly.spring.domain.review.exception;

import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.common.exception.BaseException;

public class ReviewHandler extends BaseException {
    public ReviewHandler(ErrorStatus status) {
        super(status);
    }
}
