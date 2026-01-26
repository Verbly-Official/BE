package verbly.spring.domain.correction.entity;

import jakarta.persistence.*;
import lombok.*;
import verbly.spring.domain.post.entity.Post;
import verbly.spring.global.common.entity.BaseEntity;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
@Table(name = "correction")
public class Correction extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false, unique = true)
    private Post post;

    // 즐겨찾기
    @Column(nullable = false)
    @Builder.Default
    private boolean bookmark = false;

    public void addBookmark() {
        this.bookmark = true;
    }

    public void removeBookmark() {
        this.bookmark = false;
    }
}
