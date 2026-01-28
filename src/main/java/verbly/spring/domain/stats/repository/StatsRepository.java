package verbly.spring.domain.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import verbly.spring.domain.stats.entity.Stats;

import java.util.Optional;

public interface StatsRepository extends JpaRepository<Stats, Long> {
    Optional<Stats> findByUserId(Long userId);

    @Query(value = """
        SELECT (ROUND(AVG(r.rating), 1))
        FROM Review r
        WHERE r.reviewee.id = :revieweeId
    """
    )
    Double getAverageByRevieweeId(@Param("revieweeId") Long revieweeId);

    // reviewCount 0인 경우도 포함
    @Query(value = """
        SELECT(ROUND((reviewee_ranking_list.ranking * 100.0 / reviewee_ranking_list.reviewee_amount)))
        FROM(
            SELECT stats.user_id reviewee_id,
                RANK() OVER(ORDER BY COALESCE(stats.review_average, 0) DESC) AS ranking,
                COUNT(*) OVER () AS reviewee_amount
            FROM stats stats) reviewee_ranking_list
       WHERE reviewee_ranking_list.reviewee_id = :revieweeId
    """, nativeQuery = true)
    Double getRankPercentage(@Param("revieweeId") Long revieweeId);


}
