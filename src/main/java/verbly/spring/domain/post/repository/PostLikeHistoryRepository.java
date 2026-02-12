package verbly.spring.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.post.entity.PostLikeHistory;

public interface PostLikeHistoryRepository extends JpaRepository<PostLikeHistory, Long> {
}
