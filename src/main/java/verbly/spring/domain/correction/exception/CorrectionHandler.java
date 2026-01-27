package verbly.spring.domain.correction.exception;

import verbly.spring.global.common.code.BaseErrorCode;
import verbly.spring.global.common.exception.BaseException;

public class CorrectionHandler extends BaseException {
    public CorrectionHandler(BaseErrorCode errorCode) { super(errorCode); }
}
