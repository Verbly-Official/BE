package verbly.spring.domain.post.entity;

import jakarta.persistence.*;
import lombok.*;
import verbly.spring.global.common.entity.BaseEntity;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
@Table(name = "post_like_history", indexes = @Index(columnList = "postId, recordedAt"))
public class PostLikeHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long postId;
    private int likeCount;
    private LocalDateTime recordedAt;
}
