package verbly.spring.global.common.code;

import verbly.spring.global.common.dto.ErrorReasonDTO;

public interface BaseErrorCode {
    ErrorReasonDTO getReason();

    ErrorReasonDTO getReasonHttpStatus();
}
