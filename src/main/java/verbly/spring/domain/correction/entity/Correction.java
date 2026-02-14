package verbly.spring.domain.correction.entity;

import jakarta.persistence.*;
import lombok.*;
import verbly.spring.domain.correction.enums.CorrectorType;
import verbly.spring.domain.post.entity.Post;
import verbly.spring.domain.user.entity.User;
import verbly.spring.global.common.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

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

    @Enumerated(EnumType.STRING)
    @Column
    private CorrectorType correctorType;

    @OneToMany(mappedBy = "correction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CorrectionWord> words = new ArrayList<>();

    @OneToMany(mappedBy = "correction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CorrectionFeedback> feedbacks = new ArrayList<>();

    @OneToMany(mappedBy = "correction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CorrectionBookmark> bookmarks = new ArrayList<>();


    public void markAiAssistant() {
        this.corrector = null;
        this.correctorType = CorrectorType.AI_ASSISTANT;
    }

    public void takeoverByNative(User nativeUser) {
        this.corrector = nativeUser;
        this.correctorType = CorrectorType.NATIVE_SPEAKER;
    }
}
