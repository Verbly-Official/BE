package verbly.spring.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.post.entity.Post;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByAuthorIdOrderByIdDesc(Long authorId);
    long countByAuthor_Id(Long userId);

}
