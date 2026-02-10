package verbly.spring.global.common.exception;

import lombok.Getter;
import verbly.spring.global.common.code.BaseErrorCode;
import verbly.spring.global.common.dto.ErrorReasonDTO;

@Getter
public class BaseException extends RuntimeException {
    private BaseErrorCode code;

    public BaseException(BaseErrorCode code) {
        super(code.getReason().getMessage());
        this.code = code;
    }

    public ErrorReasonDTO getErrorReason() { return this.code.getReason(); }

    public ErrorReasonDTO getErrorReasonHttpStatus() { return this.code.getReasonHttpStatus(); }
}