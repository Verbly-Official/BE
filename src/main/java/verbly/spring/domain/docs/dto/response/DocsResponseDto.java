package verbly.spring.domain.docs.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import verbly.spring.domain.docs.enums.CorrectorType;
import verbly.spring.domain.docs.enums.DocStatus;

import java.time.Instant;

@AllArgsConstructor
@Builder
public class DocsResponseDto {
    private Long id;
    private Long authorId;
    private CorrectorType correctorType;
    private Long correctorId;
    private DocStatus status;
    private String content;
    private boolean bookmarked;
    private Instant createdAt;
    private Instant updatedAt;
}
