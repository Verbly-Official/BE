package verbly.spring.domain.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import verbly.spring.domain.user.entity.User;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "chatroom_id")
    private Chatroom chatroom;

    @Column(name = "chat_content")
    private String chatContent;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
