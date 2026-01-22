package verbly.spring.domain.correction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CorrectionResponseDTO {
    private Long id;
    private String title;
    private String content;

    private boolean bookmark;
    private boolean isTemp;
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
