package verbly.spring.domain.correction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.correction.entity.Correction;

import java.util.Optional;

public interface CorrectionRepository extends JpaRepository<Correction, Long> {
    long countByPost_Author_Id(Long authorId);
    boolean existsByPostId(Long postId);
}
