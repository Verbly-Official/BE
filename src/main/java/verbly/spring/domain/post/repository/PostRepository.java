package verbly.spring.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.post.entity.Post;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByAuthorIdAndTempTrueOrderByUpdatedAtDesc(Long authorId);
    Optional<Post> findByIdAndAuthorIdAndTempTrue(Long postId, Long authorId);

}
