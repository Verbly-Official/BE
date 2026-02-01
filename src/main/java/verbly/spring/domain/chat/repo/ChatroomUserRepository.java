package verbly.spring.domain.chat.repo;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import verbly.spring.domain.chat.entity.ChatMessage;
import verbly.spring.domain.chat.entity.ChatroomUser;
import verbly.spring.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatroomUserRepository extends JpaRepository<ChatroomUser, Long> {

    boolean existsByChatroomIdAndUserId(Long chatroomId, Long userId);

    Optional<ChatroomUser> findByChatroomIdAndUserId(Long chatroomId, Long userId);

    Optional<ChatroomUser> findByUserIdAndOpponentId(Long participantId, Long opponentId);

    List<ChatroomUser> findAllByUserId(Long participantId);

    @Query("""
        SELECT chatroomUser
        FROM ChatroomUser chatroomUser, User opponent
        WHERE chatroomUser.user.id = :userId
            AND opponent.id = chatroomUser.opponentId
            AND LOWER(opponent.nickname) LIKE CONCAT('%', LOWER(:search),'%')
        ORDER BY LOWER(opponent.nickname) ASC
    """)
    List<ChatroomUser> findBySearch(@Param("userId") Long userId, @Param("search") String search);
}
