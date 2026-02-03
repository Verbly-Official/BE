package verbly.spring.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.post.entity.TrendingTag;

public interface TrendingTagRepository extends JpaRepository<TrendingTag, Long> {
}
