package verbly.spring.domain.quizreview.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import verbly.spring.domain.quizreview.enums.ReviewTaskStatus;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "review_tasks",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_review_task_item", columnNames = {"user_id", "library_item_id"})
        },
        indexes = {
                @Index(name = "idx_review_task_user_status", columnList = "user_id, status, created_at"),
                @Index(name = "idx_review_task_session", columnList = "session_id, task_order")
        }
)
public class ReviewTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK -> 사용자.Key (외부 엔티티 연결 X) */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** FK -> library_items.id (LibraryItem 엔티티는 우리 영역이므로 연결 OK) */
    @Column(name = "library_item_id", nullable = false)
    private Long libraryItemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReviewTaskStatus status = ReviewTaskStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, columnDefinition = "datetime(3)")
    private LocalDateTime createdAt;

    @Column(name = "started_at", columnDefinition = "datetime(3)")
    private LocalDateTime startedAt;

    @Column(name = "completed_at", columnDefinition = "datetime(3)")
    private LocalDateTime completedAt;

    /** FK -> review_sessions.id */
    @Column(name = "session_id")
    private Long sessionId;

    /** 세션 내 순서(1..N) */
    @Column(name = "task_order")
    private Integer taskOrder;

    public static ReviewTask pending(Long userId, Long libraryItemId) {
        ReviewTask t = new ReviewTask();
        t.userId = userId;
        t.libraryItemId = libraryItemId;
        t.status = ReviewTaskStatus.PENDING;
        return t;
    }

    public void claim(Long sessionId, int order, LocalDateTime now) {
        this.sessionId = sessionId;
        this.taskOrder = order;
        this.status = ReviewTaskStatus.IN_PROGRESS;
        this.startedAt = now;
        this.completedAt = null;
    }

    /** quit 정책: submit 안 하면 처음부터 → 다시 큐로 */
    public void rollbackToPending() {
        this.sessionId = null;
        this.taskOrder = null;
        this.status = ReviewTaskStatus.PENDING;
        this.startedAt = null;
        this.completedAt = null;
    }

    /** submit 정책: 제출하면 큐 제거 */
    public void complete(LocalDateTime now) {
        this.status = ReviewTaskStatus.COMPLETED;
        this.completedAt = now;
    }
}
