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
    USER_NEEDS_ONBOARDING(HttpStatus.CREATED, "MEMBER2001", "신규 유저입니다. 온보딩이 필요합니다."),
    USER_ALREADY_LOGIN(HttpStatus.OK, "MEMBER2002", "이미 등록된 유저입니다."),
    USER_ONBOARDING_SUCCESS(HttpStatus.CREATED, "MEMBER2003", "온보딩 정보를 성공적으로 저장했습니다."),
    USER_INFO_READ_SUCCESS(HttpStatus.OK, "MEMBER2004", "유저 정보를 성공적으로 조회했습니다."),
    USER_LOGOUT_SUCCESS(HttpStatus.OK, "MEMBER2005", "로그아웃이 완료되었습니다."),
    USER_DELETE_SUCCESS(HttpStatus.NO_CONTENT, "MEMBER2006", "회원 탈퇴가 완료되었습니다."),
    USER_PROFILE_IMAGE_UPDATED(HttpStatus.OK, "MEMBER2007", "프로필 이미지가 성공적으로 변경되었습니다."),
    USER_NICKNAME_UPDATE_SUCCESS(HttpStatus.OK, "MEMBER2008", "닉네임이 성공적으로 변경되었습니다."),
    USER_PROFILE_IMAGE_UPDATE_SUCCESS(HttpStatus.OK, "MEMBER2010", "프로필 이미지가 성공적으로 변경되었습니다."),
    USER_ALREADY_ONBOARDING_COMPLETED(HttpStatus.OK, "MEMBER2011", "온보딩이 이미 완료된 유저입니다."),
    USER_NICKNAME_CHECK_COMPLETED(HttpStatus.OK, "MEMBER2012", "사용 가능한 닉네임입니다.");

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
