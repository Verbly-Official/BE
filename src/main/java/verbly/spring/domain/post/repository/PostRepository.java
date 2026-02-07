package verbly.spring.domain.post.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import verbly.spring.domain.post.entity.Post;
import org.springframework.data.repository.query.Param;
import verbly.spring.domain.post.enums.PostStatus;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.likesCount = p.likesCount + 1 WHERE p.id = :postId")
    void increaseLikeCount(@Param("postId") Long postId);

    List<Post> findAllByAuthorIdOrderByIdDesc(Long authorId);
    long countByAuthor_Id(Long userId);
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.likesCount = p.likesCount - 1 WHERE p.id = :postId")
    void decreaseLikeCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.commentsCount = p.commentsCount + 1 WHERE p.id = :postId")
    void increaseCommentCount(@Param("postId") Long postId);

    Slice<Post> findAllByAuthor_Uuid(UUID uuid, Pageable pageable);

    List<Post> findAllByAuthorIdAndTempTrueOrderByCreatedAtDesc(Long authorId);
    Optional<Post> findByIdAndAuthorIdAndTempTrue(Long postId, Long authorId);
    Integer countByAuthorIdAndStatus(Long authorId, PostStatus status);

    Optional<Post> findByIdAndAuthorId(Long postId, Long authorId);
}
