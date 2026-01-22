package verbly.spring.domain.review.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import verbly.spring.domain.review.entity.ReviewQuestion;

import java.util.List;
import java.util.Optional;

public interface ReviewQuestionRepository extends JpaRepository<ReviewQuestion, Long> {

    /** task의 문제 전체(순서대로) */
    List<ReviewQuestion> findByReviewTask_IdOrderByQuestionOrderAsc(Long reviewTaskId);

    /** task의 n번 문제 단건 */
    Optional<ReviewQuestion> findByReviewTask_IdAndQuestionOrder(Long reviewTaskId, Integer questionOrder);

    /** task 문제 수 */
    long countByReviewTask_Id(Long reviewTaskId);

    /** (옵션) libraryItem 기준으로 문제 조회(분석/통계용) */
    List<ReviewQuestion> findByLibraryItem_IdOrderByCreatedAtDesc(Long libraryItemId);

    /** answer 제출 시 문제 잠금이 필요하면(거의 보통은 session/task 잠금으로 충분) */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ReviewQuestion> findWithLockById(Long id);

    /** (옵션) quit 시 task 단위로 문제 정리 */
    void deleteByReviewTask_Id(Long reviewTaskId);
}
