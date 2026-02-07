package verbly.spring.domain.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import verbly.spring.domain.stats.service.StatsCommandService;
import verbly.spring.domain.user.dto.response.UserResponseDTO;
import verbly.spring.domain.user.service.userhome.UserHomeQueryService;
import verbly.spring.global.common.response.ApiResponse;
import verbly.spring.global.security.auth.CustomUserDetails;
import verbly.spring.global.security.utils.SecurityUtils;

import java.util.UUID;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
public class UserHomeController implements UserControllerDocs {

    private final UserHomeQueryService userHomeQueryService;
    private final StatsCommandService statsCommandService;

    @Override
    @GetMapping("/viewer/info")
    public ApiResponse<UserResponseDTO.HomeViewerInfoDTO> getHomeViewerInfo(
    ){
        return ApiResponse.onSuccess(userHomeQueryService.getHomeViewerInfo());
    }

    @Override
    @GetMapping("/users/{uuid}")
    public ApiResponse<UserResponseDTO.HomeUserInfoDTO> getUserProfileInfo(
            @PathVariable(name = "uuid") UUID uuid
    ){
        return ApiResponse.onSuccess(userHomeQueryService.getUserProfileInfo(uuid));
    }

    @Override
    @PostMapping()
    public ResponseEntity<Void> homeApi(
            @RequestParam String timezone
    ){
        Long userId = SecurityUtils.getCurrentUserId();
        statsCommandService.markAttendance(userId, timezone);
        return ResponseEntity.noContent().build();
    }
}
