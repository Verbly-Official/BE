package verbly.spring.domain.auth.exception;

import verbly.spring.global.common.code.BaseErrorCode;
import verbly.spring.global.common.exception.BaseException;

public class AuthHandler extends BaseException {
    public AuthHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
