package verbly.spring.domain.user.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import verbly.spring.domain.user.entity.Stats;

@Repository
public interface StatsRepository extends JpaRepository<Stats, Long> {

    @Query(value = """
        SELECT ROUND(AVG(r.rating))
        FROM Review r
        WHERE r.reviewee.id = :revieweeId
    """
    )
    Double getAverageByRevieweeId(@Param("revieweeId") Long revieweeId);

    // reviewCount 0인 경우도 포함
    @Query(value = """
        SELECT(reviewee_ranking_list.rank * 100.0 / reviewee_ranking_list.reviewee_amount)
        FROM(
            SELECT stats.user_id reviewee_id,
                RANK() OVER(ORDER BY COALESCE(stats.review_average, 0) DESC) AS rank,
                COUNT(*) OVER () AS reviewee_amount
            FROM stats stats) reviewee_ranking_list
       WHERE reviewee_ranking_list.reviewee_id = :revieweeId
    """, nativeQuery = true)
    Double getRankPercentage(@Param("revieweeId") Long revieweeId);


}
