package verbly.spring.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

public class UserRequestDTO {
    @Getter
    @Setter
    public static class OnboardingDTO {
        @NotBlank(message = "학습 언어는 필수입니다.")
        @Size(max = 3, message = "학습 언어 코드를 선택해주세요.")
        @Schema(example = "en")
        private String learningLang; // "en"

        @NotEmpty(message = "모국어는 필수입니다.")
        @Size(max = 3, message = "모국어 코드를 선택해주세요.")
        @Schema(example = "kr")
        private String nativeLang; // "kr"
    }

    @Getter
    @Setter
    public static class ProfileUpdateDTO {
        @NotBlank(message = "필수 입력칸 미입력입니다. 다시 확인해주세요.")
        @Size(max = 20, message = "닉네임은 최대 20자입니다.")
        private String nickname;

//        String profileImage;

        @Size(max = 150, message = "자기소개는 최대 150자입니다.")
        private String bio;

        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Size(max = 30, message = "이메일은 최대 30자입니다.")
        private String email;

        @Pattern(
                regexp = "^[0-9+\\-]{7,20}$",
                message = "전화번호 형식이 올바르지 않습니다."
        )
        @Schema(example = "01012345678")
        private String phoneNumber;
    }
}
