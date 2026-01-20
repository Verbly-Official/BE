package verbly.spring.global.common.code;

import verbly.spring.global.common.dto.ReasonDTO;

public interface BaseCode {
    ReasonDTO getReason();

    ReasonDTO getReasonHttpStatus();
}
