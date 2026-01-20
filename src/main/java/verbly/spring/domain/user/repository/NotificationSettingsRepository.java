package verbly.spring.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.user.entity.NotificationSettings;
import verbly.spring.domain.user.entity.User;

import java.util.Optional;

public interface NotificationSettingsRepository extends JpaRepository<NotificationSettings, Long> {
    Optional<NotificationSettings> findByUser(User user);
}
