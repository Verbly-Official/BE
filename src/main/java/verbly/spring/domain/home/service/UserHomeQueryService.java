package verbly.spring.domain.home.service;

import verbly.spring.domain.home.dto.response.HomeResponseDTO;
import verbly.spring.domain.user.dto.response.UserResponseDTO;

import java.util.UUID;

public interface UserHomeQueryService {
    HomeResponseDTO.HomeViewerInfoDTO getHomeViewerInfo();
    HomeResponseDTO.HomeUserInfoDTO getUserProfileInfo(UUID uuid);
}
