package verbly.spring.domain.review.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import verbly.spring.domain.review.entity.ReviewSession;
import verbly.spring.domain.review.enums.ReviewSessionStatus;

import java.util.Optional;

public interface ReviewSessionRepository extends JpaRepository<ReviewSession, Long> {

    /** 내 세션 단건 조회(권한 체크용) */
    Optional<ReviewSession> findByIdAndUserId(Long id, Long userId);

    /**
     * submit/quit/answer 등에서 세션 상태(currentIndex/status 등) 갱신 시 동시성 방지
     * - @Query 없이도 derived method에 @Lock 적용 가능
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ReviewSession> findWithLockByIdAndUserId(Long id, Long userId);

    /** 가장 최근 IN_PROGRESS 세션 찾기(재진입 금지 정책 판단에 사용) */
    Optional<ReviewSession> findTopByUserIdAndStatusOrderByCreatedAtDesc(Long userId, ReviewSessionStatus status);

    /** (옵션) 유저의 최신 세션 */
    Optional<ReviewSession> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    /** (옵션) 유저가 현재 진행 중 세션이 있는지 */
    boolean existsByUserIdAndStatus(Long userId, ReviewSessionStatus status);
}
