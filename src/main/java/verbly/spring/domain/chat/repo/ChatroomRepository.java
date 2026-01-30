package verbly.spring.domain.chat.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import verbly.spring.domain.chat.entity.Chatroom;

@Repository
public interface ChatroomRepository extends JpaRepository<Chatroom, Long> {
}
