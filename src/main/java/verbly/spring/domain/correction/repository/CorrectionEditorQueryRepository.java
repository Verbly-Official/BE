package verbly.spring.domain.correction.repository;

import verbly.spring.domain.correction.dto.response.CorrectionEditorQueryDTO;

import java.util.List;
import java.util.Optional;

public interface CorrectionEditorQueryRepository {
    Optional<CorrectionEditorQueryDTO.CorrectionBaseRow> findCorrectionBase(Long correctionId);

    List<CorrectionEditorQueryDTO.WordRow> findWords(Long correctionId);

    List<CorrectionEditorQueryDTO.FeedbackRow> findFeedback(Long correctionId);
}
