package verbly.spring.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import verbly.spring.domain.notification.dto.response.NotificationsResponseDTO;
import verbly.spring.global.common.response.ApiResponse;

import java.util.List;

public interface NotificationControllerDocs {

    @Operation(
            summary = "실시간 알림 구독 (SSE 연결)",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = "로그인한 유저가 실시간 알림을 받기 위해 서버와 SSE 연결을 맺습니다.\n\n" +
                    "✅ **중요:**\n" +
                    "- **Content-Type:** `text/event-stream`\n" +
                    "- Swagger UI에서는 스트리밍 특성상 응답이 계속 로딩(무한 대기)으로 보일 수 있습니다.\n" +
                    "- 테스트는 **Postman**이나 **실제 프론트엔드 코드**에서 진행해주세요.\n"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "연결 성공 (이후 데이터가 스트림으로 전송됨)",
                    content = @Content(
                            mediaType = "text/event-stream",
                            examples = @ExampleObject(
                                    name = "SSE Event Example",
                                    summary = "서버에서 전송되는 이벤트 형식",
                                    value = """
                                            data: {"type": "CONNECT", "message": "EventStream Created. [userId=1]"}
                                            
                                            data: {
                                                "id": 1,
                                                "content": "박시윤님이 회원님의 게시글에 좋아요를 눌렀습니다.",
                                                "url": "/posts/123",
                                                "type": "LIKE",
                                                "isRead": false,
                                                "createdAt": "2026-02-08T17:13:46"
                                            }
                                            """
                            )
                    )
            )
    })
    SseEmitter subscribe();

    @Operation(
            summary = "알림 조회 API",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = "show all 기능을 위해 필요한 만큼 size에 입력\n\n" +
                    "✅ **요청 파라미터 (Query String):**\n" +
                    "- page: 페이지 번호 (Integer, 0부터 시작)\n" +
                    "- size: 페이지 크기 (Integer, 기본 10)\n" +
                    "- sort: 정렬 기준 (String, 기본 createdAt, DESC)\n"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "isSuccess": true,
                                                "code": "COMMON2000",
                                                "message": "성공입니다.",
                                                "result": [
                                                    {
                                                        "notificationId": 1,
                                                        "content": "박시윤 commented on your post.",
                                                        "createdAt": "2026-02-08T17:13:46.009652",
                                                        "isRead": true
                                                    }
                                                ]
                                            }
                                        """
                            )
                    )
            )
    })
    ApiResponse<List<NotificationsResponseDTO.NotificationDTO>> getNotifications(
            Pageable pageable
    );

    @Operation(
            summary = "알림 모두 읽음 API",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = "show all 기능을 위해 필요한 만큼 size에 입력\n\n" +
                    "✅ **요청 파라미터 (Query String):**\n" +
                    "- page: 페이지 번호 (Integer, 0부터 시작)\n" +
                    "- size: 페이지 크기 (Integer, 기본 10)\n" +
                    "- sort: 정렬 기준 (String, 기본 createdAt, DESC)\n"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "isSuccess": true,
                                                "code": "COMMON2000",
                                                "message": "성공입니다.",
                                                "result": [
                                                    {
                                                        "content": "박시윤 commented on your post.",
                                                        "createdAt": "2026-02-08T17:13:46.009652",
                                                        "isRead": true
                                                    }
                                                ]
                                            }
                                        """
                            )
                    )
            )
    })
    ApiResponse<List<NotificationsResponseDTO.NotificationDTO>> patchNotifications(
            Pageable pageable
    );

    @Operation(
            summary = "알림 읽음 API",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = "✅ **요청 파라미터 (Query String):**\n" +
                    "- notificationId: 알림 ID ( Long )"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "isSuccess": true,
                                                "code": "COMMON2000",
                                                "message": "성공입니다.",
                                                "result": {
                                                    "notificationId": 29,
                                                    "content": "유진 liked your post.",
                                                    "createdAt": "2026-02-14T00:41:17",
                                                    "isRead": true
                                                }
                                            }
                                        """
                            )
                    )
            )
    })
    ApiResponse<NotificationsResponseDTO.NotificationDTO> patchNotification(
            @PathVariable(name = "notificationId") Long notificationId
    );
}
