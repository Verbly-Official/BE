package verbly.spring.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_settings")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class NotificationSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private boolean emailNotify;

    @Column(nullable = false)
    private boolean pushNotify;

    @Column(nullable = false)
    private boolean correctionNotify;

    public static NotificationSettings defaultOf(User user) {
        return NotificationSettings.builder()
                .user(user)
                .emailNotify(true)
                .pushNotify(true)
                .correctionNotify(true)
                .build();
    }

    public void updateEmail(boolean value) {
        this.emailNotify = value;
    }

    public void updatePush(boolean value) {
        this.pushNotify = value;
    }

    public void updateCorrection(boolean value) {
        this.correctionNotify = value;
    }
}
