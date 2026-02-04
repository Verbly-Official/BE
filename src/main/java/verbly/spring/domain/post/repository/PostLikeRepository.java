package verbly.spring.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.post.entity.Post;
import verbly.spring.domain.post.entity.PostLike;
import verbly.spring.domain.user.entity.User;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByUserAndPost(User viewer, Post post);
    Optional<PostLike> findByUserAndPost(User user, Post post);
}
