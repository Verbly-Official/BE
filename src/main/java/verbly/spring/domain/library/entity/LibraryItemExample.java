package verbly.spring.domain.library.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import verbly.spring.domain.library.enums.ExampleSource;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "library_item_examples",
        indexes = {
                @Index(name = "idx_examples_item", columnList = "library_item_id")
        }
)
public class LibraryItemExample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK -> library_items.id */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_item_id", nullable = false)
    private LibraryItem libraryItem;

    @Lob
    @Column(name = "example_en", nullable = false, columnDefinition = "TEXT")
    private String exampleEn;

    @Lob
    @Column(name = "example_ko", columnDefinition = "TEXT")
    private String exampleKo;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private ExampleSource source;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, columnDefinition = "datetime(3)")
    private LocalDateTime createdAt;

    public static LibraryItemExample of(LibraryItem item, String exampleEn, String exampleKo, ExampleSource source) {
        LibraryItemExample e = new LibraryItemExample();
        e.libraryItem = item;
        e.exampleEn = exampleEn;
        e.exampleKo = exampleKo;
        e.source = source;
        return e;
    }
}