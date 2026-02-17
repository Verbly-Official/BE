package verbly.spring.domain.notification.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import verbly.spring.domain.notification.entity.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.receiver.id = :userId")
    void bulkRead(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.receiver.id =:userId AND n.id =:notificationId")
    void read(@Param("userId") Long userId, @Param("notificationId") Long notificationId);

    Slice<Notification> findByReceiver_IdOrderByCreatedAtDesc(Long receiverId, Pageable pageable);

    Notification findByReceiver_IdAndId(Long userId, Long notificationId);
}
