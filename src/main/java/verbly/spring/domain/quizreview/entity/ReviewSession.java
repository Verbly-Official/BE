package verbly.spring.domain.quizreview.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import org.hibernate.annotations.CreationTimestamp;
import verbly.spring.domain.quizreview.enums.ReviewSessionStatus;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "review_sessions",
        indexes = {
                @Index(name = "idx_review_sessions_user_status", columnList = "user_id, status, created_at")
        }
)
public class ReviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK -> 사용자.Key (외부 테이블) */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReviewSessionStatus status = ReviewSessionStatus.IN_PROGRESS;

    @Column(name = "total_tasks", nullable = false)
    private int totalTasks;


    @Column(name = "current_index", nullable = false)
    private int currentIndex = 1;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, columnDefinition = "datetime(3)")
    private LocalDateTime createdAt;

    @Column(name = "completed_at", columnDefinition = "datetime(3)")
    private LocalDateTime completedAt;

    @Column(name = "quit_at", columnDefinition = "datetime(3)")
    private LocalDateTime quitAt;
    @Column(name = "hint_total", nullable = false)
    private int hintTotal = 2;

    @Column(name = "hint_used", nullable = false)
    private int hintUsed = 0;

    public static ReviewSession start(Long userId, int totalTasks) {
        ReviewSession s = new ReviewSession();
        s.userId = userId;
        s.totalTasks = totalTasks;
        s.currentIndex = 1;
        s.status = ReviewSessionStatus.IN_PROGRESS;
        s.hintTotal = 2;
        s.hintUsed = 0;
        return s;
    }

    public void advance() {
        this.currentIndex = Math.min(this.currentIndex + 1, this.totalTasks);
    }

    public void complete(LocalDateTime now) {
        this.status = ReviewSessionStatus.COMPLETED;
        this.completedAt = now;
    }
    public void useHint() {
        if (hintUsed >= hintTotal) {
            throw new IllegalStateException("No hints remaining");
        }
        hintUsed += 1;
    }

    public void quit(LocalDateTime now) {
        this.status = ReviewSessionStatus.QUIT;
        this.quitAt = now;
    }
}