package verbly.spring.domain.docs.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import verbly.spring.domain.docs.enums.CorrectorType;
import verbly.spring.domain.docs.enums.DocStatus;
import verbly.spring.domain.user.entity.User;
import verbly.spring.global.common.entity.BaseEntity;


@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "docs")
public class Document extends BaseEntity {

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author; // 작성자

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
    private boolean bookmark;

}
