package verbly.spring.domain.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import verbly.spring.domain.notification.dto.response.NotificationsResponseDTO;
import verbly.spring.domain.notification.service.NotificationCommandService;
import verbly.spring.domain.notification.service.NotificationQueryService;
import verbly.spring.domain.notification.service.NotificationService;
import verbly.spring.global.common.response.ApiResponse;
import verbly.spring.global.security.utils.SecurityUtils;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationRestController implements NotificationControllerDocs {

    private final NotificationService notificationService;
    private final NotificationQueryService notificationQueryService;
    private final NotificationCommandService notificationCommandService;

    @Override
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        Long userId = SecurityUtils.getCurrentUserId();
        return notificationService.subscribe(userId);
    }

    @Override
    @GetMapping()
    public ApiResponse<List<NotificationsResponseDTO.NotificationDTO>> getNotifications(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.onSuccess(notificationQueryService.getNotifications(userId, pageable));
    }

    @Override
    @PatchMapping()
    public ApiResponse<List<NotificationsResponseDTO.NotificationDTO>> patchNotifications(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.onSuccess(notificationCommandService.patchNotifications(userId, pageable));
    }

    @Override
    @PatchMapping("/{notificationId}")
    public ApiResponse<NotificationsResponseDTO.NotificationDTO> patchNotification(
            @PathVariable(name = "notificationId") Long notificationId
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.onSuccess(notificationCommandService.patchNotification(userId ,notificationId));
    }
}