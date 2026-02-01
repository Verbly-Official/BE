package verbly.spring.domain.quizreview.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.quizreview.entity.ReviewQuestion;

import java.util.List;
import java.util.Optional;

public interface ReviewQuestionRepository extends JpaRepository<ReviewQuestion, Long> {

    @EntityGraph(attributePaths = {"reviewTask", "libraryItem"})
    Optional<ReviewQuestion> findWithTaskAndItemById(Long id);

    Optional<ReviewQuestion> findTopByReviewTask_IdOrderByQuestionOrderAsc(Long reviewTaskId);

    List<ReviewQuestion> findAllByReviewTask_IdIn(List<Long> reviewTaskIds);

    void deleteAllByReviewTask_IdIn(List<Long> reviewTaskIds);
}
