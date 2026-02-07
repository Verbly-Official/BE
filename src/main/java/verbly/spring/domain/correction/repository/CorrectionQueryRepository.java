package verbly.spring.domain.correction.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import verbly.spring.domain.correction.dto.response.CorrectionResponseDTO;
import verbly.spring.domain.correction.enums.CorrectorType;
import verbly.spring.domain.post.enums.PostStatus;

import java.util.List;

public interface CorrectionQueryRepository {
    List<CorrectionResponseDTO.MyCorrectionDto> findMyCorrections(
            Long authorId,
            Boolean bookmark,
            Boolean sort,
            PostStatus status,
            CorrectorType correctorType
    );

    Page<CorrectionResponseDTO.MyCorrectionDto> findNativeCorrectionRequests(PostStatus status, Pageable pageable);

    long countMyCorrections(
            Long userId,
            Boolean bookmark,
            PostStatus status,
            CorrectorType correctorType
    );

    long countNativeCorrectionRequests(PostStatus status);

}
