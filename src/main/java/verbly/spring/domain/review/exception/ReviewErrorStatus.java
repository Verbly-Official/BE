package verbly.spring.domain.review.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import verbly.spring.global.common.code.BaseErrorCode;
import verbly.spring.global.common.dto.ErrorReasonDTO;

@Getter
@AllArgsConstructor
public enum ReviewErrorStatus implements BaseErrorCode {

    QUIZ_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "QUIZ4001", "퀴즈 세션을 찾을 수 없습니다."),
    QUIZ_SESSION_NOT_IN_PROGRESS(HttpStatus.BAD_REQUEST, "QUIZ4002", "진행 중인 퀴즈 세션이 아닙니다."),
    QUIZ_QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "QUIZ4003", "퀴즈 문항을 찾을 수 없습니다."),
    QUIZ_FORBIDDEN(HttpStatus.FORBIDDEN, "QUIZ4004", "해당 퀴즈에 접근 권한이 없습니다."),
    QUIZ_OUT_OF_ORDER(HttpStatus.BAD_REQUEST, "QUIZ4005", "현재 순서의 문제만 풀 수 있습니다."),
    QUIZ_NO_PENDING_ITEMS(HttpStatus.BAD_REQUEST, "QUIZ4006", "리뷰할 항목이 없습니다."),
    QUIZ_NO_MISTAKES(HttpStatus.BAD_REQUEST, "QUIZ4007", "오답이 없어 재도전할 수 없습니다."),
    QUIZ_NO_HINTS_REMAINING(HttpStatus.BAD_REQUEST, "QUIZ4008", "남은 힌트가 없습니다.");
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .httpStatus(httpStatus)
                .isSuccess(false)
                .code(code)
                .message(message)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return getReason();
    }
}
