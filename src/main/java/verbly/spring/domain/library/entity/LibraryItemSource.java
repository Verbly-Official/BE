package verbly.spring.domain.library.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import org.hibernate.annotations.CreationTimestamp;
import verbly.spring.domain.library.enums.SourceStatus;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "library_item_sources",
        indexes = {
                @Index(name = "idx_sources_item", columnList = "library_item_id"),
                @Index(name = "idx_sources_post", columnList = "post_id"),
                @Index(name = "idx_sources_correction", columnList = "correction_id")
        }
)
public class LibraryItemSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK -> library_items.id */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_item_id", nullable = false)
    private LibraryItem libraryItem;

    /** 외부 테이블 FK들(우린 엔티티로 묶지 않고 ID만 보관) */
    @Column(name = "post_id")
    private Long postId;               // 글.Key

    @Column(name = "correction_id")
    private Long correctionId;         // 커렉션.Key

    @Column(name = "feedback_id")
    private Long feedbackId;           // 커렉션 피드백.Key

    @Column(name = "correction_word_id")
    private Long correctionWordId;     // 커렉션 단어.Key

    /** 원문 위치 정보(옵션) */
    @Column(name = "sentence_index")
    private Integer sentenceIndex;

    @Column(name = "token_start")
    private Integer tokenStart;

    @Column(name = "token_end")
    private Integer tokenEnd;

    /** 원문/교정문 구간 */
    @Lob
    @Column(name = "original_segment", columnDefinition = "TEXT")
    private String originalSegment;

    @Lob
    @Column(name = "suggestion_segment", columnDefinition = "TEXT")
    private String suggestionSegment;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_status", nullable = false, length = 20)
    private SourceStatus sourceStatus = SourceStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, columnDefinition = "datetime(3)")
    private LocalDateTime createdAt;

    public static LibraryItemSource of(LibraryItem item) {
        LibraryItemSource s = new LibraryItemSource();
        s.libraryItem = item;
        s.sourceStatus = SourceStatus.ACTIVE;
        return s;
    }

    public void archive() {
        this.sourceStatus = SourceStatus.ARCHIVED;
    }
}
