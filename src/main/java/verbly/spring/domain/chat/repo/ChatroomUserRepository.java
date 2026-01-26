package verbly.spring.domain.chat.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import verbly.spring.domain.chat.entity.ChatroomUser;
import verbly.spring.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatroomUserRepository extends JpaRepository<ChatroomUser, Long> {

    boolean existsByChatroomIdAndUserId(Long chatroomId, Long userId);

    Optional<ChatroomUser> findByUserIdAndOpponentId(Long participantId, Long opponentId);

    List<ChatroomUser> findAllByUserId(Long participantId);

    Optional<ChatroomUser> findByUserId(Long participantId);
}
