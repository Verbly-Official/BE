package verbly.spring.domain.post.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
@Table(name = "hot_post")
public class HotPost {
    @Id
    private Long postId;

    private int growthScore;

    @OneToOne
    @MapsId
    @JoinColumn(name = "post_id")
    private Post post;
}
