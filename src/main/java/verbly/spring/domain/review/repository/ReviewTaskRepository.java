package verbly.spring.domain.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.review.entity.ReviewTask;
import verbly.spring.domain.review.enums.ReviewTaskStatus;

import java.util.List;
import java.util.Optional;

public interface ReviewTaskRepository extends JpaRepository<ReviewTask, Long> {

    boolean existsByUserIdAndLibraryItemId(Long userId, Long libraryItemId);

    List<ReviewTask> findAllByUserId(Long userId);

    List<ReviewTask> findAllByUserIdAndStatusOrderByCreatedAtAscIdAsc(Long userId, ReviewTaskStatus status);

    List<ReviewTask> findAllBySessionIdOrderByTaskOrderAsc(Long sessionId);

    List<ReviewTask> findAllBySessionId(Long sessionId);

    Optional<ReviewTask> findBySessionIdAndTaskOrder(Long sessionId, Integer taskOrder);
}
