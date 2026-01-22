package verbly.spring.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.post.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
}
