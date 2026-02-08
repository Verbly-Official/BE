package verbly.spring.domain.notification.service;

import org.springframework.data.domain.Pageable;
import verbly.spring.domain.notification.dto.response.NotificationsResponseDTO;

import java.util.List;

public interface NotificationCommandService {
    List<NotificationsResponseDTO.NotificationDTO> patchNotifications(Long userId, Pageable pageable);
}
