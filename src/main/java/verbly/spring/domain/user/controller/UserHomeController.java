package verbly.spring.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import verbly.spring.domain.user.dto.response.UserResponseDTO;
import verbly.spring.domain.user.service.userhome.UserHomeQueryService;
import verbly.spring.global.common.response.ApiResponse;
import verbly.spring.global.security.auth.CustomUserDetails;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
public class UserHomeController implements UserControllerDocs {

    private final UserHomeQueryService userHomeQueryService;

    @Override
    @GetMapping()
    public ApiResponse<UserResponseDTO.HomeViewerInfoDTO> getHomeViewerInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        return ApiResponse.onSuccess(userHomeQueryService.getHomeViewerInfo(userDetails));
    }
}
