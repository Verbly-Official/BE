package verbly.spring.domain.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import verbly.spring.domain.notification.dto.NotificationResponseDTO;
import verbly.spring.domain.notification.entity.Notification;
import verbly.spring.domain.notification.enums.NotificationType;
import verbly.spring.domain.notification.repository.EmitterRepository;
import verbly.spring.domain.notification.repository.NotificationRepository;
import verbly.spring.domain.user.entity.User;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmitterRepository emitterRepository;
    private final NotificationRepository notificationRepository;

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterRepository.save(userId, emitter);

        emitter.onCompletion(() -> emitterRepository.deleteById(userId));
        emitter.onTimeout(() -> emitterRepository.deleteById(userId));

        sendToClient(emitter, userId, "EventStream Created. [userId=" + userId + "]");

        return emitter;
    }

    @Transactional
    public void send(User receiver, User sender, NotificationType type, String url) {
        String content = createNotificationContent(sender, type);

        Notification notification = notificationRepository.save(
                Notification.builder()
                        .receiver(receiver)
                        .sender(sender)
                        .content(content)
                        .notificationType(type)
                        .url(url)
                        .isRead(false)
                        .build()
        );

        Long receiverId = receiver.getId();
        SseEmitter emitter = emitterRepository.get(receiverId);

        if (emitter != null) {
            sendToClient(emitter, receiverId, NotificationResponseDTO.from(notification));
        }
    }

    private void sendToClient(SseEmitter emitter, Long id, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .id(String.valueOf(id))
                    .name("sse")
                    .data(data));
        } catch (Exception e) {
            emitterRepository.deleteById(id);
            emitter.completeWithError(e);
        }
    }

    private String createNotificationContent(User sender, NotificationType type) {
        if (sender == null) {
            return "새로운 알림이 도착했습니다.";
        }

        String nickname = sender.getNickname();

        switch (type) {
            case LIKE:
                return nickname + " liked your post.";
            case COMMENT:
                return nickname + " commented on your post.";
            case FOLLOW:
                return nickname + " started following you.";
            default:
                return nickname + " sent you a notification.";
        }
    }
}