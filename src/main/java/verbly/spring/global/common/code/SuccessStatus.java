package verbly.spring.global.common.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import verbly.spring.global.common.dto.ReasonDTO;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseCode {
    // 일반적인 응답
    _OK(HttpStatus.OK, "COMMON2000", "성공입니다."),

    // 멤버 관련 응답
    USER_NEEDS_ONBOARDING(HttpStatus.CREATED, "USER2001", "신규 유저입니다. 온보딩이 필요합니다."),
    USER_ALREADY_LOGIN(HttpStatus.OK, "USER2002", "이미 등록된 유저입니다."),
    USER_ONBOARDING_SUCCESS(HttpStatus.CREATED, "USER2003", "온보딩 정보를 성공적으로 저장했습니다."),
    USER_INFO_READ_SUCCESS(HttpStatus.OK, "USER2004", "유저 정보를 성공적으로 조회했습니다."),
    USER_LOGOUT_SUCCESS(HttpStatus.OK, "USER2005", "로그아웃이 완료되었습니다."),
    USER_DELETE_SUCCESS(HttpStatus.NO_CONTENT, "USER2006", "회원 탈퇴가 완료되었습니다."),
    USER_PROFILE_UPDATE_SUCCESS(HttpStatus.OK, "USER2007", "프로필이 성공적으로 변경되었습니다."),
    USER_ALREADY_ONBOARDING_COMPLETED(HttpStatus.OK, "USER2011", "온보딩이 이미 완료된 유저입니다."),

    CORRECTION_CREATE_SUCCESS(HttpStatus.CREATED, "CORRECTION2001", "Correction - 글을 성공적으로 저장했습니다."),
    CORRECTION_READ_SUCCESS(HttpStatus.OK, "CORRECTION2002", "Correction - 글을 성공적으로 조회했습니다."),
    CORRECTION_UPDATE_SUCCESS(HttpStatus.OK, "CORRECTION2003", "Correction - 글을 성공적으로 수정했습니다."),
    CORRECTION_DELETE_SUCCESS(HttpStatus.OK, "CORRECTION2004", "Correction - 글을 성공적으로 삭제했습니다."),
    CORRECTION_BOOKMARK_ADD_SUCCESS(HttpStatus.OK, "CORRECTION2005", "Correction - 글을 성공적으로 즐겨찾기에 추가했습니다."),
    CORRECTION_BOOKMARK_REMOVE_SUCCESS(HttpStatus.OK, "CORRECTION2006", "Correction - 글을 즐겨찾기에서 성공적으로 삭제했습니다"),
    CORRECTION_TEMP_CREATE_SUCCESS(HttpStatus.CREATED, "CORRECTION2007", "Correction - 글을 성공적으로 임시저장했습니다."),
    CORRECTION_TEMP_UPDATE_SUCCESS(HttpStatus.OK, "CORRECTION2008", "Correction - 임시저장 글을 성공적으로 수정했습니다."),
    CORRECTION_TEMP_READ_SUCCESS(HttpStatus.OK, "CORRECTION2009", "Correction - 임시저장 글을 성공적으로 조회했습니다."),
    CORRECTION_TEMP_DELETE_SUCCESS(HttpStatus.OK, "CORRECTION2010", "Correction - 임시저장 글을 성공적으로 삭제했습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ReasonDTO getReason() {
        return ReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(true)
                .build();
    }

    @Override
    public ReasonDTO getReasonHttpStatus() {
        return ReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(true)
                .httpStatus(httpStatus)
                .build();
    }
}
