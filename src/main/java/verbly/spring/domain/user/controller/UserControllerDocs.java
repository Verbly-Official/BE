package verbly.spring.domain.user.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import verbly.spring.domain.user.dto.response.UserResponseDTO;
import verbly.spring.global.common.response.ApiResponse;
import verbly.spring.global.security.auth.CustomUserDetails;

import java.util.UUID;

public interface UserControllerDocs {

    ApiResponse<UserResponseDTO.HomeViewerInfoDTO> getHomeViewerInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    );

    ApiResponse<UserResponseDTO.HomeUserInfoDTO> getUserProfileInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable(name = "uuid") UUID uuid
    );
}
