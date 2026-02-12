package verbly.spring.domain.correction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.correction.entity.CorrectionBookmark;

import java.util.Optional;

public interface CorrectionBookmarkRepository extends JpaRepository<CorrectionBookmark, Long> {
    boolean existsByUserIdAndCorrectionId(Long userId, Long correctionId);

    Optional<CorrectionBookmark> findByUserIdAndCorrectionId(Long userId, Long correctionId);

    void deleteByUserIdAndCorrectionId(Long userId, Long correctionId);

    void deleteAllByCorrection_Id(Long correctionId);

}
