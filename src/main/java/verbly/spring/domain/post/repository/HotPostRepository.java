package verbly.spring.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.post.entity.HotPost;

public interface HotPostRepository extends JpaRepository<HotPost, Long> {
}
