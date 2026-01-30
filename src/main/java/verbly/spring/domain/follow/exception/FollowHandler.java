package verbly.spring.domain.follow.exception;

import verbly.spring.global.common.code.BaseErrorCode;
import verbly.spring.global.common.exception.BaseException;

public class FollowHandler extends BaseException {
    public FollowHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
