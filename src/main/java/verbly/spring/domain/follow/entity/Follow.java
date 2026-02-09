package verbly.spring.domain.follow.entity;

import jakarta.persistence.*;
import lombok.*;
import verbly.spring.domain.user.entity.User;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(access = AccessLevel.PRIVATE)
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "follower_id")
    private User follower;

    @ManyToOne
    @JoinColumn(name = "followee_id")
    private User followee;

    @Column(name = "followed_at")
    private LocalDateTime followedAt;

    public static Follow of (User follower, User followee) {

        return Follow.builder()
                .follower(follower)
                .followee(followee)
                .followedAt(LocalDateTime.now())
                .build();
    }
}
