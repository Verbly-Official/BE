package verbly.spring.domain.correction.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "correction_word")
public class CorrectionWord{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private Correction correction;

    // 문장 idx
    @Column(nullable = false)
    private Integer sentenceIdx;

    // 문장에서 수정할 블록 시작 idx
    @Column(nullable = false)
    private Integer startIdx;

    // 문장에서 수정할 블록 끝 idx
    @Column(nullable = false)
    private Integer endIdx;

    // 원문
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String originalText;

    // 교정된 문자열
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String correctedText;
}
