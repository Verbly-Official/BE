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
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER4001", "사용자가 존재하지 않습니다."),
    NICKNAME_NOT_EXIST(HttpStatus.BAD_REQUEST, "USER4002", "닉네임은 필수입니다."),
    ONBOARDING_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "USER4003", "온보딩을 마치지 않았습니다."),
    SOCIALID_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER4005", "socialId가 없습니다."),
    INVALID_IMAGE_TYPE(HttpStatus.BAD_REQUEST, "IMAGE4006", "이미지 형식의 파일만 업로드할 수 있습니다."),
    IMAGE_FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "IMAGE4007", "이미지 파일은 10MB 이하로 업로드해주세요."),
    NICKNAME_DUPLICATE(HttpStatus.BAD_REQUEST, "USER4008", "이미 사용 중인 닉네임입니다."),
    USER_STATS_NOT_FOUND(HttpStatus.NOT_FOUND, "USER4009", "사용자의 상태 정보를 찾을 수 없습니."),
    ONBOARDING_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "USER4009", "이미 온보딩이 완료된 유저입니다."),

    //jwt 토큰
    INVALID_JWT_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN4001", "유효하지 않은 AccessToken입니다."),
    INVALID_JWT_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN4002", "유효하지 않은 RefreshToken입니다."),
    INVALID_SOCIAL_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN4003", "유효하지 않은 SocialToken입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "PW4001", "잘못된 비밀번호입니다."),

    // 팔로우
    NOT_FOLLOWED_USER(HttpStatus.BAD_REQUEST, "FOLLOW4001", "팔로우하지 않은 사용자입니다."),
    CANT_SELF_FOLLOW(HttpStatus.BAD_REQUEST, "FOLLOW4002", "스스로 팔로우 할 수 없습니다."),
    ALREADY_FOLLOWED(HttpStatus.BAD_REQUEST, "FOLLOW4003", "이미 팔로우한 사용자입니다."),

    //채팅 토큰
    URI_PATH_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT4001", "URI를 찾을 수 없습니다."),
    WEBSOCKET_SESSION_CLOSED(HttpStatus.BAD_REQUEST, "CHAT4002", "웹소켓 세션이 닫혔습니다."),
    CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT4003", "채팅방이 없습니다."),
    NOT_CHATROOM_MEMBER(HttpStatus.FORBIDDEN, "CHAT4004", "채팅방 입장 권한이 없습니다."),
    CANT_SELF_CHAT(HttpStatus.BAD_REQUEST, "CHAT4004", "스스로 채팅할 수 없습니다."),

    // Correction 관련 에러
    CORRECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "CORRECTION4001", "문서를 찾을 수 없습니다."),
    CORRECTION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CORRECTION4002", "해당 문서에 대한 권한이 없습니다."),
    CORRECTION_NOT_VALIDATE(HttpStatus.BAD_REQUEST, "CORRECTION4003", "제목과 내용은 필수 입력 항목입니다."),
    CORRECTION_TEMP_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "CORRECTION4004", "임시저장 문서를 찾을 수 없습니다."),
    CORRECTION_TEMP_POST_ALREADY_SUBMITTED(HttpStatus.BAD_REQUEST, "CORRECTION4005", "이미 Correction 요청한 문서입니다."),
    CORRECTION_NATIVE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CORRECTION4006", "Correction Native - 접근 권한이 없습니다."),
    CORRECTION_WORD_NOT_FOUND(HttpStatus.NOT_FOUND, "CORRECTION4007", "교정 대상 단어를 찾을 수 없습니다."),

    // 통계 관련 에러
    STATS_NOT_FOUND(HttpStatus.BAD_REQUEST, "STATS4001", "사용자 통계 정보가 존재하지 않습니다."),

    //라이브러리 관련 에러
    LIBRARY_ITEM_NOT_FOUND(HttpStatus.BAD_REQUEST, "LIB4001", "라이브러리 아이템을 찾을 수 없습니다."),
    LIBRARY_ITEM_FORBIDDEN(HttpStatus.FORBIDDEN, "LIB4002", "해당 아이템에 대한 권한이 없습니다."),
    LIBRARY_ITEM_DUPLICATE(HttpStatus.BAD_REQUEST, "LIB4003", "이미 라이브러리에 저장된 표현입니다."),
    EXAMPLE_NOT_FOUND(HttpStatus.BAD_REQUEST, "LIB4004", "예문을 찾을 수 없습니다."),

    //리뷰(퀴즈) 관련 에러
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
