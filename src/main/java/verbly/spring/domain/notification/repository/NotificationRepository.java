package verbly.spring.domain.notification.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.notification.entity.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Slice<Notification> findByReceiver_IdOrderByCreatedAtDesc(Long receiverId, Pageable pageable);
}
