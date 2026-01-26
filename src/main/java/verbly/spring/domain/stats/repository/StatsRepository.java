package verbly.spring.domain.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.stats.entity.Stats;

import java.util.Optional;

public interface StatsRepository extends JpaRepository<Stats, Long> {
    Optional<Stats> findByUserId(Long userId);
}
