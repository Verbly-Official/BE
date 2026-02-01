package verbly.spring.domain.quizreview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.quizreview.entity.ReviewAnswer;

import java.util.List;
import java.util.Optional;

public interface ReviewAnswerRepository extends JpaRepository<ReviewAnswer, Long> {

    Optional<ReviewAnswer> findTopByReviewQuestion_IdOrderByAttemptNoDesc(Long reviewQuestionId);

    List<ReviewAnswer> findAllByReviewQuestion_IdInOrderByReviewQuestion_IdAscAttemptNoDesc(List<Long> reviewQuestionIds);
}
