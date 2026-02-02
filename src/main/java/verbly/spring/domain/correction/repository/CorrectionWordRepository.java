package verbly.spring.domain.correction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.correction.entity.CorrectionWord;

public interface CorrectionWordRepository extends JpaRepository<CorrectionWord, Long> {
    @Transactional
    void deleteByCorrectionId(Long correctionId);
}
