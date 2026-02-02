package verbly.spring.domain.chat.exception;

import verbly.spring.global.common.code.BaseErrorCode;
import verbly.spring.global.common.exception.BaseException;

public class ChatHandler extends BaseException {
    public ChatHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
