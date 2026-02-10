package verbly.spring.domain.chat.repo;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import verbly.spring.domain.chat.entity.ChatMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("""
        SELECT message
        FROM ChatMessage message
        WHERE message.chatroom.id = :chatroomId
            AND message.createdAt >
                (
                    SELECT chatroomUser.joinedAt
                    FROM ChatroomUser chatroomUser
                    WHERE chatroomUser.user.id = :userId
                        AND chatroomUser.chatroom.id = :chatroomId
                    )
    """)
    List<ChatMessage> findAllBySenderIdAndChatroomIdOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("chatroomId") Long chatroomId);

    Optional<ChatMessage> findTopByChatroomIdOrderByCreatedAtDesc(Long chatroomId);

    @Query("""
        SELECT message
        FROM ChatMessage message
        WHERE message.chatroom.id IN (
            SELECT chatroomUser.chatroom.id
            FROM ChatroomUser chatroomUser
            WHERE chatroomUser.user.id = :userId
        )
        AND LOWER(message.chatContent) LIKE CONCAT('%', LOWER(:search), '%')
        ORDER BY message.createdAt DESC
""")
    List<ChatMessage> findMessageBySearch(@Param("userId") Long userId, @Param("search") String search);

    @Query("""
        SELECT COUNT(message)
        FROM ChatMessage message
        WHERE message.chatroom.id = :chatroomId
            AND message.sender.id <> :userId
            AND message.createdAt > :lastReadAt
    """)
    Integer countUnreadChatMessage(@Param("userId") Long userId, @Param("chatroomId") Long chatroomId, @Param("lastReadAt") LocalDateTime lastReadAt);
}
