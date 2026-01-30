package verbly.spring.domain.chat.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import verbly.spring.domain.chat.entity.ChatroomUser;

@Repository
public interface ChatroomUserRepository extends JpaRepository<ChatroomUser, Long> {
}
