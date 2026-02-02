package verbly.spring.global.webSocket.exception;

import verbly.spring.global.common.code.BaseErrorCode;
import verbly.spring.global.common.exception.BaseException;

public class WebSocketExceptionHandler extends BaseException {

    public WebSocketExceptionHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}