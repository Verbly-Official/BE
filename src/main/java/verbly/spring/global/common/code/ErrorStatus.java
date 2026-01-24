package verbly.spring.global.common.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import verbly.spring.global.common.dto.ErrorReasonDTO;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {
    // 가장 일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON5000", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON4000","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON4001","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON4003", "금지된 요청입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "COMMON4004", "권한이 없습니다."),

    // 유저 관련 에러
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER4001", "사용자가 없습니다."),
    NICKNAME_NOT_EXIST(HttpStatus.BAD_REQUEST, "USER4002", "닉네임은 필수입니다."),
    ONBOARDING_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "USER4003", "온보딩을 마치지 않았습니다."),
    SOCIALID_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER4005", "socialId가 없습니다."),
    INVALID_IMAGE_TYPE(HttpStatus.BAD_REQUEST, "IMAGE4006", "이미지 형식의 파일만 업로드할 수 있습니다."),
    IMAGE_FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "IMAGE4007", "이미지 파일은 10MB 이하로 업로드해주세요."),
    NICKNAME_DUPLICATE(HttpStatus.BAD_REQUEST, "USER4008", "이미 사용 중인 닉네임입니다."),
    USER_STATS_NOT_FOUND(HttpStatus.NOT_FOUND, "USER4009", "사용자의 상태 정보를 찾을 수 없습니다."),

    //jwt 토큰
    INVALID_JWT_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN4001", "유효하지 않은 AccessToken입니다."),
    INVALID_JWT_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN4002", "유효하지 않은 RefreshToken입니다."),
    INVALID_SOCIAL_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN4003", "유효하지 않은 SocialToken입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "PW4001", "잘못된 비밀번호입니다."),

    //채팅 토큰
    URI_PATH_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT4001", "URI를 찾을 수 없습니다."),
    WEBSOCKET_SESSION_CLOSED(HttpStatus.BAD_REQUEST, "CHAT4002", "웹소켓 세션이 닫혔습니다."),
    CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT4003", "채팅방이 없습니다."),
    NOT_CHATROOM_MEMBER(HttpStatus.FORBIDDEN, "CHAT4004", "채팅방 입장 권한이 없습니다.");

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
