package verbly.spring.domain.user.service.userhome;

import verbly.spring.domain.user.dto.response.UserResponseDTO;
import verbly.spring.domain.user.entity.User;
import verbly.spring.global.security.auth.CustomUserDetails;

import java.util.UUID;

public interface UserHomeQueryService {
    UserResponseDTO.HomeViewerInfoDTO getHomeViewerInfo(CustomUserDetails userDetails);
    UserResponseDTO.HomeUserInfoDTO getUserProfileInfo(CustomUserDetails userDetails, UUID uuid);
}
