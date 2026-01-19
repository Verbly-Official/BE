package verbly.spring.domain.library.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import verbly.spring.domain.library.enums.LibraryItemStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "library_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_library_item_dedupe",
                        columnNames = {"user_id", "phrase_norm"}
                )
        },
        indexes = {
                @Index(name = "idx_library_items_user_updated", columnList = "user_id, updated_at, id"),
                @Index(name = "idx_library_items_user_status", columnList = "user_id, status")
        }
)
public class LibraryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK -> 사용자.Key (외부 테이블) */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 화면에 보여줄 표현 */
    @Column(name = "phrase", nullable = false, length = 255)
    private String phrase;

    /** 중복 방지용 정규화된 표현(lower/trim/space normalize 등) */
    @Column(name = "phrase_norm", nullable = false, length = 255)
    private String phraseNorm;

    @Lob
    @Column(name = "meaning_ko", columnDefinition = "TEXT")
    private String meaningKo;

    @Lob
    @Column(name = "meaning_en", columnDefinition = "TEXT")
    private String meaningEn;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LibraryItemStatus status = LibraryItemStatus.ACTIVE;

    @Column(name = "is_starred", nullable = false)
    private boolean starred = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, columnDefinition = "datetime(3)")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "datetime(3)")
    private LocalDateTime updatedAt;

    // ---------- relations (우리 테이블 내에서만) ----------
    @OneToMany(mappedBy = "libraryItem", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LibraryItemSource> sources = new ArrayList<>();

    @OneToMany(mappedBy = "libraryItem", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LibraryItemExample> examples = new ArrayList<>();

    // ---------- factory / behavior ----------
    public static LibraryItem of(Long userId, String phrase, String phraseNorm, String meaningKo, String meaningEn) {
        LibraryItem item = new LibraryItem();
        item.userId = userId;
        item.phrase = phrase;
        item.phraseNorm = phraseNorm;
        item.meaningKo = meaningKo;
        item.meaningEn = meaningEn;
        item.status = LibraryItemStatus.ACTIVE;
        item.starred = false;
        return item;
    }

    public void toggleStar(boolean starred) {
        this.starred = starred;
    }

    public void softDelete() {
        this.status = LibraryItemStatus.DELETED;
    }
}