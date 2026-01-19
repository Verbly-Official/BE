package verbly.spring.domain.docs.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import verbly.spring.domain.docs.enums.CorrectorType;
import verbly.spring.domain.docs.enums.DocStatus;

import java.time.Instant;

@AllArgsConstructor
@Builder
public class DocsListItemResponseDto {
    private Long id;
    private DocStatus status;
    private CorrectorType correctorType;
    private boolean bookmarked;
    private Instant createdAt;
    private Instant updatedAt;
}
