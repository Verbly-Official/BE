package verbly.spring.domain.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
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

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // 60분

    // [1] SSE 구독 (로그인 시 연결)
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterRepository.save(userId, emitter);

        // 연결 종료/타임아웃 시 자동 삭제
        emitter.onCompletion(() -> emitterRepository.deleteById(userId));
        emitter.onTimeout(() -> emitterRepository.deleteById(userId));

        // 503 에러 방지용 더미 데이터 전송
        sendToClient(emitter, userId, "EventStream Created. [userId=" + userId + "]");

        return emitter;
    }

    // [2] 알림 전송 (외부에서 호출)
    @Transactional
    public void send(User receiver, User sender, NotificationType type, String url) {
        // 1. 메시지 생성 (Sender 닉네임 포함)
        String content = createNotificationContent(sender, type);

        // 2. DB 저장 (필수)
        Notification notification = notificationRepository.save(
                Notification.builder()
                        .receiver(receiver)
                        .sender(sender)
                        .content(content) // "siyoon liked your post" 저장됨
                        .notificationType(type)
                        .url(url)
                        .isRead(false)
                        .build()
        );

        // 3. 실시간 전송 (접속 중일 때만)
        Long receiverId = receiver.getId();
        SseEmitter emitter = emitterRepository.get(receiverId);

        if (emitter != null) {
            // Notification 객체나 DTO를 그대로 보내면 프론트에서 처리하기 편함
            sendToClient(emitter, receiverId, notification);
        }
    }

    // ⭐️ [핵심] 알림 메시지 생성 로직
    private String createNotificationContent(User sender, NotificationType type) {
        // 시스템 알림일 경우 Sender가 없을 수 있음
        if (sender == null) {
            return "새로운 알림이 도착했습니다.";
        }

        String nickname = sender.getNickname();

        switch (type) {
            case LIKE:
                return nickname + " liked your post."; // "siyoon liked your post."
            case COMMENT:
                return nickname + " commented on your post.";
            case FOLLOW:
                return nickname + " started following you.";
            default:
                return nickname + " sent you a notification.";
        }
    }

    // 내부 전송 메서드
    private void sendToClient(SseEmitter emitter, Long id, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .id(String.valueOf(id))
                    .name("sse")
                    .data(data));
        } catch (IOException e) {
            emitterRepository.deleteById(id);
            emitter.completeWithError(e);
        }
    }
}