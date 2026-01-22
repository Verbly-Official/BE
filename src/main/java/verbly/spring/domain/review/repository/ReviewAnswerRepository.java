package verbly.spring.domain.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.review.entity.ReviewAnswer;

import java.util.List;
import java.util.Optional;

public interface ReviewAnswerRepository extends JpaRepository<ReviewAnswer, Long> {

    /** 문제의 모든 시도(오답/정답 포함) */
    List<ReviewAnswer> findByReviewQuestion_IdOrderByAttemptNoAsc(Long reviewQuestionId);

    /** 특정 attempt 조회 */
    Optional<ReviewAnswer> findByReviewQuestion_IdAndAttemptNo(Long reviewQuestionId, Integer attemptNo);

    /** attempt 중복 방지 체크 */
    boolean existsByReviewQuestion_IdAndAttemptNo(Long reviewQuestionId, Integer attemptNo);

    // ------------------------
    // 오답노트(유저별) - @Query 없이도 가능(연관관계 필드명이 아래와 같다는 전제)
    // ReviewAnswer.reviewQuestion -> ReviewQuestion.reviewTask -> ReviewTask.userId
    // ------------------------
    List<ReviewAnswer> findByReviewQuestion_ReviewTask_UserIdAndIsCorrectFalseOrderByAnsweredAtDesc(
            Long userId
    );

    /** 오답노트: 최근 N개 잘라 쓰려면 Service에서 Pageable로 바꾸는 걸 추천 */
    // Page<ReviewAnswer> findByReviewQuestion_ReviewTask_UserIdAndIsCorrectFalse(Long userId, Pageable pageable);

    /** quit/삭제 시 question 단위 정리 */
    void deleteByReviewQuestion_Id(Long reviewQuestionId);

    /** quit/삭제 시 task 단위 정리(연관관계 기반) */
    void deleteByReviewQuestion_ReviewTask_Id(Long reviewTaskId);

    /** quit/삭제 시 session 단위 정리(연관관계 기반) */
    void deleteByReviewQuestion_ReviewTask_Session_Id(Long sessionId);
}
