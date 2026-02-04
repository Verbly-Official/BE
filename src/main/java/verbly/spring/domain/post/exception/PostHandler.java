package verbly.spring.domain.post.exception;

import verbly.spring.global.common.code.BaseErrorCode;
import verbly.spring.global.common.exception.BaseException;

public class PostHandler extends BaseException {
    public PostHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
