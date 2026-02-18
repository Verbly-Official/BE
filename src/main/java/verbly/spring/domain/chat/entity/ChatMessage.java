package verbly.spring.domain.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import verbly.spring.domain.user.entity.User;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(access = AccessLevel.PRIVATE)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = true)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "chatroom_id")
    private Chatroom chatroom;

    @Column(name = "chat_content")
    private String chatContent;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public static ChatMessage of(User sender, Chatroom chatroom, String chatContent) {

        return ChatMessage.builder()
                .sender(sender)
                .chatroom(chatroom)
                .chatContent(chatContent)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
