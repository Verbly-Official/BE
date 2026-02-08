package verbly.spring.domain.notification.service;

import org.springframework.data.domain.Pageable;
import verbly.spring.domain.notification.dto.response.NotificationsResponseDTO;

import java.util.List;

public interface NotificationQueryService {
    List<NotificationsResponseDTO.NotificationDTO> getNotifications(Long userId, Pageable pageable);
}
