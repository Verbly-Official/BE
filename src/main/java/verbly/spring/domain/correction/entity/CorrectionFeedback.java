package verbly.spring.domain.correction.entity;

import jakarta.persistence.*;
import lombok.*;
import verbly.spring.domain.correction.enums.CorrectorType;
import verbly.spring.domain.user.entity.User;
import verbly.spring.global.common.entity.BaseEntity;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
@Table(name = "correction_feedback")
public class CorrectionFeedback extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "correction_id", nullable = false)
    private Correction correction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "correction_edit_id", nullable = false)
    private CorrectionWord correctionWord;

    // 코멘트 작성자 = 도움 준 사람
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "corrector_id", nullable = false)
    private User corrector;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CorrectorType correctorType;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
}
