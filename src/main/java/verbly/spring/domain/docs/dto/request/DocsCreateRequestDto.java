package verbly.spring.domain.docs.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import verbly.spring.domain.docs.enums.CorrectorType;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocsCreateRequestDto {
    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @NotNull
    private CorrectorType correctorType;

    // correctorType=USER일 때
    private Long correctorId;
}
