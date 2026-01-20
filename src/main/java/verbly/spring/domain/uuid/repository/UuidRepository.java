package verbly.spring.domain.uuid.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import verbly.spring.domain.uuid.entity.Uuid;

@Repository
public interface UuidRepository extends JpaRepository<Uuid, Long> {
}
