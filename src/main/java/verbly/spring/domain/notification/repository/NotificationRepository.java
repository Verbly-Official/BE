package verbly.spring.domain.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.notification.entity.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findTop4ByReceiver_IdOrderByCreatedAtDesc(Long receiverId);
    List<Notification> findTop10ByReceiver_IdOrderByCreatedAtDesc(Long receiverId);
}
