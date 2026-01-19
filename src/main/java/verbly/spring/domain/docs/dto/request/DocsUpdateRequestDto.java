package verbly.spring.domain.docs.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import verbly.spring.domain.docs.enums.DocStatus;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocsUpdateRequestDto {
    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private DocStatus status;
}
