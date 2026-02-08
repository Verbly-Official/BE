package verbly.spring.domain.notification.service;

import verbly.spring.domain.notification.dto.response.NotificationsResponseDTO;

import java.util.List;

public interface NotificationQueryService {
    List<NotificationsResponseDTO.NotificationDTO> getNotifications(Long userId);
}
