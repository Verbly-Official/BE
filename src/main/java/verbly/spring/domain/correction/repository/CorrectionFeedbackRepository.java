package verbly.spring.domain.correction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.correction.entity.CorrectionFeedback;

import java.util.List;
import java.util.Optional;

public interface CorrectionFeedbackRepository extends JpaRepository<CorrectionFeedback, Long> {
    void deleteAllByCorrectionId(Long correctionId);
    long countByCorrector_Id(Long correctorId);
    long countDistinctCorrectionByCorrectorId(Long correctorId);
    Optional<CorrectionFeedback> findTopByCorrectionIdOrderByCreatedAtDesc(Long correctionId);
    boolean existsByCorrectionId(Long correctionId);
    List<CorrectionFeedback> findAllByCorrectionIdOrderByCreatedAtAsc(Long correctionId);
}
