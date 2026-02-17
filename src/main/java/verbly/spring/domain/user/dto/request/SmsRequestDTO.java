package verbly.spring.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

public class SmsRequestDTO {
    @Getter
    @Setter
    public static class SendDTO {
        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(
                regexp = "^01[016789]\\d{7,8}$",
                message = "전화번호 형식이 올바르지 않습니다."
        )
        @Schema(example = "01012345678")
        private String phoneNumber;
    }

    @Getter
    @Setter
    public static class VerifyDTO {
        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(
                regexp = "^01[016789]\\d{7,8}$",
                message = "전화번호 형식이 올바르지 않습니다."
        )
        @Schema(example = "01012345678")
        private String phoneNumber;

        @NotBlank(message = "인증번호는 필수입니다.")
        @Size(min = 6, max = 6, message = "인증번호는 6자리입니다.")
        private String code;
    }
}
