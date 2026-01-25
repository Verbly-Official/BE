package verbly.spring.domain.correction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.correction.entity.Correction;

import java.util.Optional;

public interface CorrectionRepository extends JpaRepository<Correction, Long> {
    Optional<Correction> findByPostId(Long postId);
    boolean existsByPostId(Long postId);
}
