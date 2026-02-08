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

    // 코멘트 작성자 = 도움 준 사람
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "corrector_id", nullable = false)
    private User corrector;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CorrectorType correctorType;

    @Column(nullable = false)
    private Integer sentenceIdx;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    public void updateContent(String content) {
        this.content = content;
    }

}
