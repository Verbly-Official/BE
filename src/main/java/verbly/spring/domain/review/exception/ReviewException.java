package verbly.spring.domain.review.exception;

import verbly.spring.global.common.exception.BaseException;

public class ReviewException extends BaseException {
    public ReviewException(ReviewErrorStatus status) {
        super(status);
    }
}
