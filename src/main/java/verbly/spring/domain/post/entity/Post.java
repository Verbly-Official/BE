package verbly.spring.domain.post.entity;

import jakarta.persistence.*;
import lombok.*;
import verbly.spring.domain.post.enums.PostStatus;
import verbly.spring.domain.user.entity.User;
import verbly.spring.global.common.entity.BaseEntity;
import org.hibernate.annotations.Check;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
@Table(name = "post")
@Check(constraints = "likes_count >= 0 AND comments_count >= 0")
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // 상태 (TEMP, PENDING, IN_PROGRESS, COMPLETED)
    // TEMP 인 경우: 임시저장
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PostStatus status;

    // 제목
    @Column(nullable = false)
    private String title;

    // 내용
    @Lob
    @Column(nullable = false)
    private String content;

    // 임시저장 유무
    @Column(nullable = false)
    private boolean temp;

    @Builder.Default
    @Column(nullable = false)
    private Integer likesCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer commentsCount = 0;

    @Column(nullable = false)
    private Boolean publicSetting;

    private LocalDateTime correctedAt;

    @Builder.Default
    private Boolean hotPosted = false;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostTag> postTags = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> postLikes = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();


    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public boolean isSameContent(String title, String content) {
        return this.title.equals(title) && this.content.equals(content);
    }

    public void changeStatus(PostStatus status) {
        this.status = status;
    }

    public void changeTemp(boolean temp) {
        this.temp = temp;
    }

}
