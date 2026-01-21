package verbly.spring.domain.correction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;


import java.time.LocalDateTime;

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
