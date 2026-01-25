package verbly.spring.domain.correction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.correction.entity.CorrectionFeedback;

public interface CorrectionFeedbackRepository extends JpaRepository<CorrectionFeedback, Long> {
    void deleteAllByCorrectionId(Long correctionId);
}
