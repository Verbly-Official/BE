package verbly.spring.domain.correction.repository;

import verbly.spring.domain.correction.dto.response.CorrectionResponseDTO;
import verbly.spring.domain.correction.enums.CorrectorType;
import verbly.spring.domain.post.enums.PostStatus;

import java.util.List;
import java.util.Optional;

public interface CorrectionQueryRepository {
    List<CorrectionResponseDTO.MyCorrectionDto> findMyCorrections(
            Long authorId,
            Boolean bookmark,
            Boolean sort,
            PostStatus status,
            CorrectorType correctorType
    );

    Optional<CorrectionResponseDTO.MyCorrectionDto> findCorrectionDetail(
            Long authorId,
            Long correctionId
    );
}
