package verbly.spring.domain.quizreview.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "review_answers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_review_attempt", columnNames = {"review_question_id", "attempt_no"})
        }
)
public class ReviewAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * FK -> review_questions.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_question_id", nullable = false)
    private ReviewQuestion reviewQuestion;

    @Column(name = "attempt_no", nullable = false)
    private int attemptNo = 1;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "user_answer_json", nullable = false, columnDefinition = "json")
    private JsonNode userAnswerJson;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;



    @CreationTimestamp
    @Column(name = "answered_at", nullable = false, columnDefinition = "datetime(3)")
    private LocalDateTime answeredAt;

    public static ReviewAnswer of(ReviewQuestion q, int attemptNo, JsonNode userAnswerJson, boolean correct) {
        ReviewAnswer a = new ReviewAnswer();
        a.reviewQuestion = q;
        a.attemptNo = attemptNo;
        a.userAnswerJson = userAnswerJson;
        a.correct = correct;
        return a;
    }

}
