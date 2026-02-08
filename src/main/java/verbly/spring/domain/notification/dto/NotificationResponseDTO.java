package verbly.spring.domain.notification.dto;

import lombok.Builder;
import lombok.Getter;
import verbly.spring.domain.notification.entity.Notification;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponseDTO {
    private Long id;
    private String content;
    private String url;
    private String type;
    private LocalDateTime createdAt;

    public static NotificationResponseDTO from(Notification notification) {
        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .content(notification.getContent())
                .url(notification.getUrl())
                .type(notification.getNotificationType().name())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
