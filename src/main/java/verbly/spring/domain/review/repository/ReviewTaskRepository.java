package verbly.spring.domain.review.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import verbly.spring.domain.review.entity.ReviewTask;
import verbly.spring.domain.review.enums.ReviewTaskStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReviewTaskRepository extends JpaRepository<ReviewTask, Long> {

    /** 내 task 단건 조회 */
    Optional<ReviewTask> findByIdAndUserId(Long id, Long userId);

    /** (중요) 유저+아이템에 대한 task(중복 방지/상태 확인) */
    Optional<ReviewTask> findByUserIdAndLibraryItem_Id(Long userId, Long libraryItemId);

    /** 라이브러리 페이지(12개)에서 해당 itemIds의 리뷰상태를 한 번에 가져올 때(IN) */
    List<ReviewTask> findByUserIdAndLibraryItem_IdIn(Long userId, Collection<Long> libraryItemIds);

    /** 특정 세션의 task 전체(순서대로) */
    List<ReviewTask> findBySession_IdOrderByTaskOrderAsc(Long sessionId);

    /** 세션에서 특정 order의 task */
    Optional<ReviewTask> findBySession_IdAndTaskOrder(Long sessionId, Integer taskOrder);

    /** 세션의 다음 진행할 task(상태 기준) */
    Optional<ReviewTask> findFirstBySession_IdAndStatusOrderByTaskOrderAsc(Long sessionId, ReviewTaskStatus status);

    /** 세션의 남은 task 개수 등 집계용 */
    long countBySession_IdAndStatusIn(Long sessionId, Collection<ReviewTaskStatus> statuses);

    /** 큐(퀴즈 목록) 조회용: PENDING/IN_PROGRESS */
    List<ReviewTask> findByUserIdAndStatusInOrderByCreatedAtAsc(Long userId, Collection<ReviewTaskStatus> statuses);

    /** (재진입 금지 구현 시) 진행 중 task가 있는지 */
    boolean existsBySession_IdAndStatusIn(Long sessionId, Collection<ReviewTaskStatus> statuses);

    /**
     * submit/quit 시 task를 안전하게 바꿔야 하면 잠금 사용 가능
     * - 세션 단위로 잠그는 게 더 일반적이지만, 필요 시 task 단건에도 적용 가능
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ReviewTask> findWithLockByIdAndUserId(Long id, Long userId);

    /** (옵션) quit 시 세션 task 정리 */
    void deleteBySession_Id(Long sessionId);
}
