package verbly.spring.domain.home.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import verbly.spring.domain.home.dto.response.HomeResponseDTO;
import verbly.spring.domain.home.service.UserHomeQueryService;
import verbly.spring.domain.stats.service.StatsCommandService;
import verbly.spring.global.common.response.ApiResponse;
import verbly.spring.global.security.utils.SecurityUtils;

import java.util.UUID;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
public class HomeController implements HomeControllerDocs {

    private final UserHomeQueryService userHomeQueryService;
    private final StatsCommandService statsCommandService;

    @Override
    @GetMapping("/viewer/info")
    public ApiResponse<HomeResponseDTO.HomeViewerInfoDTO> getHomeViewerInfo(
    ){
        return ApiResponse.onSuccess(userHomeQueryService.getHomeViewerInfo());
    }

    @Override
    @GetMapping("/users/{uuid}")
    public ApiResponse<HomeResponseDTO.HomeUserInfoDTO> getUserProfileInfo(
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
        statsCommandService.checkAttendance(userId, timezone);
        return ResponseEntity.noContent().build();
    }
}
