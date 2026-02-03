package verbly.spring.domain.quizreview.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;
import verbly.spring.domain.library.entity.LibraryItem;
import verbly.spring.domain.quizreview.enums.ReviewQuestionType;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "review_questions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_review_question_order", columnNames = {"review_task_id", "question_order"})
        },
        indexes = {
                @Index(name = "idx_rq_task", columnList = "review_task_id"),
                @Index(name = "idx_rq_item", columnList = "library_item_id")
        }
)
public class ReviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK -> review_tasks.id */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_task_id", nullable = false)
    private ReviewTask reviewTask;

    /** FK -> library_items.id (조인 최적화용 redundant) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_item_id", nullable = false)
    private LibraryItem libraryItem;

    @Column(name = "question_order", nullable = false)
    private int questionOrder = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 20)
    private ReviewQuestionType questionType;

    @Lob
    @Column(name = "prompt", nullable = false, columnDefinition = "TEXT")
    private String prompt;

    /** 보기(선택): ["a","b","c"] */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options_json", columnDefinition = "json")
    private JsonNode optionsJson;

    /** 정답/채점키(필수) */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "answer_key_json", nullable = false, columnDefinition = "json")
    private JsonNode answerKeyJson;

    @Lob
    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    /** 외부 FK: */
    @Column(name = "source_post_id")
    private Long sourcePostId;

    @Lob
    @Column(name = "hint",columnDefinition = "TEXT")
    private String hint;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, columnDefinition = "datetime(3)")
    private LocalDateTime createdAt;

    public static ReviewQuestion of(ReviewTask task, LibraryItem item, int order,
                                    ReviewQuestionType type, String prompt,
                                    JsonNode optionsJson, JsonNode answerKeyJson) {
        ReviewQuestion q = new ReviewQuestion();
        q.reviewTask = task;
        q.libraryItem = item;
        q.questionOrder = order;
        q.questionType = type;
        q.prompt = prompt;
        q.optionsJson = optionsJson;
        q.answerKeyJson = answerKeyJson;

        return q;
    }


}