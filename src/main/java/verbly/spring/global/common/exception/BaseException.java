package verbly.spring.global.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import verbly.spring.global.common.code.BaseErrorCode;
import verbly.spring.global.common.dto.ErrorReasonDTO;

@Getter
@AllArgsConstructor
public class BaseException extends RuntimeException {
    private BaseErrorCode code;

    public ErrorReasonDTO getErrorReason() { return this.code.getReason(); }

    public ErrorReasonDTO getErrorReasonHttpStatus() { return this.code.getReasonHttpStatus(); }
}
