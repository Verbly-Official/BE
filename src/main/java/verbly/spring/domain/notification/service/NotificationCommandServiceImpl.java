package verbly.spring.domain.notification.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import verbly.spring.domain.notification.converter.NotificationConverter;
import verbly.spring.domain.notification.dto.response.NotificationsResponseDTO;
import verbly.spring.domain.notification.entity.Notification;
import verbly.spring.domain.notification.repository.NotificationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationCommandServiceImpl implements NotificationCommandService {

    private final NotificationRepository notificationRepository;

    @Override
    public List<NotificationsResponseDTO.NotificationDTO> patchNotifications(Long userId, Pageable pageable) {
        notificationRepository.bulkRead(userId);
        Slice<Notification> notifications = notificationRepository.findByReceiver_IdOrderByCreatedAtDesc(userId, pageable);
        return notifications.stream()
                .map(NotificationConverter::toNotificationDTO)
                .collect(Collectors.toList());
    }
}
