package verbly.spring.domain.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import verbly.spring.domain.user.entity.User;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "chatroom_user")
public class ChatroomUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn
    private User user;

    @ManyToOne
    @JoinColumn
    private Chatroom chatroom;

    @Column(name = "unread_chat_count")
    private Integer unreadChatCount;

    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "quit_at")
    private LocalDateTime quitAt;


}
