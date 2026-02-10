package verbly.spring.domain.correction.entity;

import jakarta.persistence.*;
import lombok.*;
import verbly.spring.domain.post.entity.Post;
import verbly.spring.domain.user.entity.User;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corrector_id")
    private User corrector;

    public void assignCorrector(User user) {
        this.corrector = user;
    }
}
