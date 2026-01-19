package verbly.spring.domain.docs.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "docs")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @ManyToOne(fetch = FetchType.LAZY, optional = false)
     * @JoinColumn(name = "author_id", nullable = false)
     * private User author;
     *
     * @Column(name = "corrector_id")
     * private Long corrector;
     **/

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CorrectorType correctorType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DocStatus status;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private boolean isTemp;

    @Column(nullable = false)
    private boolean bookmarked;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateStatus(DocStatus status) {
        this.status = status;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

    public void setTemp(boolean temp) {
        isTemp = temp;
    }


//    public void setCorrector(CorrectorType type, Long correctorId) {
//        this.correctorType = type;
//        this.correctorId = correctorId;
//    }
}
