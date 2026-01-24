package verbly.spring.domain.chat.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import verbly.spring.domain.chat.entity.ChatroomUser;

import java.time.LocalDateTime;

@Repository
public interface ChatroomUserRepository extends JpaRepository<ChatroomUser, Long> {
    boolean existsByChatroomIdAndUserIdAndQuitAtIsNull(Long chatroomId, Long userId);
}
