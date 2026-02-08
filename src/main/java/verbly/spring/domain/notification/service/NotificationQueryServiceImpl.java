package verbly.spring.domain.notification.service;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.ErrorState;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.notification.converter.NotificationConverter;
import verbly.spring.domain.notification.dto.response.NotificationResponseDTO;
import verbly.spring.domain.notification.dto.response.NotificationsResponseDTO;
import verbly.spring.domain.notification.entity.Notification;
import verbly.spring.domain.notification.repository.NotificationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryServiceImpl implements NotificationQueryService{
    private final NotificationRepository notificationRepository;

    @Override
    public List<NotificationsResponseDTO.NotificationDTO> getNotifications(Long userId, Pageable pageable) {
        Slice<Notification> notifications = notificationRepository.findByReceiver_IdOrderByCreatedAtDesc(userId, pageable);
        return notifications.stream()
                .map(NotificationConverter::toNotificationDTO)
                .collect(Collectors.toList());
    }
}
