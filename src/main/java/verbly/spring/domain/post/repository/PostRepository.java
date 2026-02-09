package verbly.spring.domain.post.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import verbly.spring.domain.post.entity.Post;
import org.springframework.data.repository.query.Param;
import verbly.spring.domain.post.enums.PostStatus;

import java.time.LocalDateTime;
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

    @Query(value = """
        SELECT p.id, 
               (p.likes_count - COALESCE(
                   (SELECT h.like_count 
                    FROM post_like_history h 
                    WHERE h.post_id = p.id AND h.recorded_at <= :cutoffTime 
                    ORDER BY h.recorded_at DESC LIMIT 1), 0)
               ) as growth
        FROM post p
        WHERE p.status = 'COMPLETED' AND p.likes_count >= :minLikes
        ORDER BY growth DESC
        LIMIT 10
        """, nativeQuery = true)
    List<Object[]> findTop10RisingPosts(
            @Param("minLikes") int minLikes,
            @Param("cutoffTime") LocalDateTime cutoffTime
    );

    List<Post> findByStatusAndLikesCountGreaterThanEqual(PostStatus postStatus, int i);
}
