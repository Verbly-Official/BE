package verbly.spring.domain.chat.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import verbly.spring.domain.chat.entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
}
