package verbly.spring.domain.correction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.correction.entity.CorrectionWord;

import java.util.List;

public interface CorrectionWordRepository extends JpaRepository<CorrectionWord, Long> {
    List<CorrectionWord> findByCorrectionId(Long correctionId);

    @Transactional
    void deleteByCorrectionId(Long correctionId);

    interface CorrectionCountRow {
        Long getCorrectionId();
        Long getCnt();
    }

    @Query("""
        select cw.correction.id as correctionId, count(cw.id) as cnt
        from CorrectionWord cw
        where cw.correction.id in :correctionIds
        group by cw.correction.id
    """)
    List<CorrectionCountRow> countWordsByCorrectionIds(@Param("correctionIds") List<Long> correctionIds);

    @Query("""
        select cw.correction.id as correctionId, count(cw.id) as cnt
        from CorrectionWord cw
        where cw.correction.id in :correctionIds
          and cw.originalText <> cw.correctedText
        group by cw.correction.id
    """)
    List<CorrectionCountRow> countChangedWordsByCorrectionIds(@Param("correctionIds") List<Long> correctionIds);
}
