package verbly.spring.domain.notification.converter;

import verbly.spring.domain.notification.dto.response.NotificationsResponseDTO;
import verbly.spring.domain.notification.entity.Notification;

public class NotificationConverter {
    public static NotificationsResponseDTO.NotificationDTO toNotificationDTO(Notification notification) {
        return NotificationsResponseDTO.NotificationDTO.builder()
                .content(notification.getContent())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    public static
}
