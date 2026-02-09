package verbly.spring.domain.quizreview.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import verbly.spring.domain.quizreview.entity.ReviewSession;
import verbly.spring.domain.quizreview.enums.ReviewSessionStatus;

import java.util.Optional;

public interface ReviewSessionRepository extends JpaRepository<ReviewSession, Long> {

    Optional<ReviewSession> findByIdAndUserId(Long id, Long userId);

    Optional<ReviewSession> findTopByUserIdAndStatusOrderByCreatedAtDesc(Long userId, ReviewSessionStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select s from ReviewSession s
        where s.id = :sessionId and s.userId = :userId
    """)
    Optional<ReviewSession> findByIdAndUserIdForUpdate(
            @Param("sessionId") Long sessionId,
            @Param("userId") Long userId
    );
}
