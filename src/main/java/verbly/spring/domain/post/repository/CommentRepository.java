package verbly.spring.domain.post.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.post.entity.Comment;
import verbly.spring.domain.post.entity.Post;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Slice<Comment> findByPost(Pageable pageable, Post post);
}
