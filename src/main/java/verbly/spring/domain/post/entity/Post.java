package verbly.spring.domain.post.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;
import verbly.spring.domain.post.enums.Status;
import verbly.spring.global.common.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "posts")
@Check(constraints = "likes_count >= 0 AND comments_count >= 0")
public class Post extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private Boolean isTemp;

    @Column(nullable = false)
    private Integer likesCount;

    @Column(nullable = false)
    private Integer commentsCount;

    @Column(nullable = false)
    private Boolean helpNeeded;
}
