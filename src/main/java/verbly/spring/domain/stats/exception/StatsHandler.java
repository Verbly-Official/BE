package verbly.spring.domain.stats.exception;

import verbly.spring.global.common.code.BaseErrorCode;
import verbly.spring.global.common.exception.BaseException;

public class StatsHandler extends BaseException {
    public StatsHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
