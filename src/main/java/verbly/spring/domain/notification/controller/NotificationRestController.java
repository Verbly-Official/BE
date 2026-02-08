package verbly.spring.domain.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import verbly.spring.domain.notification.dto.response.NotificationsResponseDTO;
import verbly.spring.domain.notification.service.NotificationQueryService;
import verbly.spring.domain.notification.service.NotificationService;
import verbly.spring.global.common.response.ApiResponse;
import verbly.spring.global.security.utils.SecurityUtils;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationRestController {

    private final NotificationService notificationService;
    private final NotificationQueryService notificationQueryService;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        Long userId = SecurityUtils.getCurrentUserId();
        return notificationService.subscribe(userId);
    }

    @GetMapping()
    public ApiResponse<List<NotificationsResponseDTO.NotificationDTO>> getNotifications() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.onSuccess(notificationQueryService.getNotifications(userId));
    }

    @GetMapping("/all")
    public ApiResponse<List<NotificationsResponseDTO.NotificationDTO>> getAllNotifications() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.onSuccess(notificationQueryService.getNotifications10(userId));
    }
}