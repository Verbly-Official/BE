package verbly.spring.domain.post.entity;

import jakarta.persistence.*;
import lombok.*;
import verbly.spring.domain.post.enums.PostStatus;
import verbly.spring.domain.user.entity.User;
import verbly.spring.global.common.entity.BaseEntity;


@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
@Table(name = "post")
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // 상태 (PENDING, IN_PROGRESS, COMPLETED)
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

    // 즐겨찾기
    @Column(nullable = false)
    private boolean bookmark;

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public boolean isSameContent(String title, String content) {
        return this.title.equals(title) && this.content.equals(content);
    }

}
