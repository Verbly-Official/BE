package verbly.spring.domain.post.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import verbly.spring.domain.post.entity.Post;
import verbly.spring.domain.post.entity.PostTag;
import verbly.spring.domain.post.entity.Tag;

import java.time.LocalDateTime;
import java.util.List;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {
    @Query("SELECT pt.tag " +
            "FROM PostTag pt " +
            "WHERE pt.createdAt BETWEEN :start AND :end " +
            "GROUP BY pt.tag " +
            "ORDER BY COUNT(pt) DESC")
    List<Tag> findTrendingTagIds(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    void deleteByPost(Post post);

    interface PostIdTagRow {
        Long getPostId();
        Long getPostTagId();
        String getTagName();
    }

    @Query("""
        select pt.post.id as postId,
               pt.id as postTagId,
               t.name as tagName
        from PostTag pt
        join pt.tag t
        where pt.post.id in :postIds
        order by pt.post.id asc, pt.id asc
    """)
    List<PostIdTagRow> findPostIdTagRows(@Param("postIds") List<Long> postIds);
}
