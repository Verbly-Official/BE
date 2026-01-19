package verbly.spring.domain.library.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import verbly.spring.global.common.code.BaseErrorCode;
import verbly.spring.global.common.dto.ErrorReasonDTO;

@Getter
@AllArgsConstructor
public enum LibraryErrorStatus implements BaseErrorCode {

    LIBRARY_ITEM_NOT_FOUND(HttpStatus.BAD_REQUEST, "LIB4001", "라이브러리 아이템을 찾을 수 없습니다."),
    LIBRARY_ITEM_FORBIDDEN(HttpStatus.FORBIDDEN, "LIB4002", "해당 아이템에 대한 권한이 없습니다."),
    LIBRARY_ITEM_DUPLICATE(HttpStatus.BAD_REQUEST, "LIB4003", "이미 라이브러리에 저장된 표현입니다."),
    EXAMPLE_NOT_FOUND(HttpStatus.BAD_REQUEST, "LIB4004", "예문을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build();
    }
}
