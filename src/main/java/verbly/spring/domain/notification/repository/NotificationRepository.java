package verbly.spring.domain.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
