package verbly.spring.domain.correction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.correction.entity.CorrectionWord;

import java.util.List;

public interface CorrectionWordRepository extends JpaRepository<CorrectionWord, Long> {
    List<CorrectionWord> findByCorrectionId(Long correctionId);

    @Transactional
    void deleteByCorrectionId(Long correctionId);
}
