package verbly.spring.domain.follow.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import verbly.spring.domain.follow.entity.Follow;
import verbly.spring.domain.user.entity.User;
import java.util.List;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    @Query("""
        SELECT user
        FROM User user
        WHERE user.id <> :followerId
             AND user.nativeLang = 'en'
            AND NOT EXISTS (
                SELECT 1
                FROM Follow follow
                WHERE follow.follower.id = :followerId
                    AND follow.followee.id = user.id
            )
        ORDER BY FUNCTION('rand')
     """)
    List<User> findRandomUser(@Param("followerId") Long followerId, Pageable pageable);
    void deleteByFollowerIdAndFolloweeId(Long followerId, Long followeeId);
    boolean existsFollowByFollowerIdAndFolloweeId(Long followerId, Long followeeId);
    long countByFollowerId(Long followerId); // 내가 팔로우한 수
    long countByFolloweeId(Long followeeId); // 나를 팔로우한 수 // 추후 확장할 수도
}