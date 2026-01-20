package verbly.spring.domain.user.exception;

import verbly.spring.global.common.code.BaseErrorCode;
import verbly.spring.global.common.exception.BaseException;

public class UserHandler extends BaseException {
    public UserHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
