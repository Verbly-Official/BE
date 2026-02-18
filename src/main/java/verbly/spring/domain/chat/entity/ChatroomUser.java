package verbly.spring.domain.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import verbly.spring.domain.user.entity.User;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "chatroom_user")
public class ChatroomUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @ManyToOne
    @JoinColumn(name = "chatroom_id")
    private Chatroom chatroom;

    @Column(name = "opponent_id")
    private Long opponentId;

    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    public static ChatroomUser of(User user, Chatroom chatroom, Long opponentId) {

        return ChatroomUser.builder()
                .user(user)
                .chatroom(chatroom)
                .opponentId(opponentId)
                .lastReadAt(LocalDateTime.now())
                .joinedAt(LocalDateTime.now())
                .build();
    }

    public void updateLastReadAt(LocalDateTime lastReadAt) {
        this.lastReadAt = lastReadAt;
    }
}
