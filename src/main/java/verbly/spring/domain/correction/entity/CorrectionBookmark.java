package verbly.spring.domain.correction.entity;

import jakarta.persistence.*;
import lombok.*;
import verbly.spring.domain.user.entity.User;
import verbly.spring.global.common.entity.BaseEntity;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
@Table(
        name = "correction_bookmark",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cb_user_correction",
                columnNames = {"user_id", "correction_id"}
        ),
        indexes = {
                @Index(name = "idx_cb_user", columnList = "user_id"),
                @Index(name = "idx_cb_correction", columnList = "correction_id")
        }
)
public class CorrectionBookmark extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "correction_id", nullable = false)
    private Correction correction;
}
