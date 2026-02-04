package verbly.spring.domain.user.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import verbly.spring.domain.user.dto.response.UserResponseDTO;
import verbly.spring.global.common.response.ApiResponse;
import verbly.spring.global.security.auth.CustomUserDetails;

public interface UserControllerDocs {

    ApiResponse<UserResponseDTO.HomeViewerInfoDTO> getHomeViewerInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    );
}
