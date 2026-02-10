package verbly.spring.domain.home.service;

import verbly.spring.domain.home.dto.response.HomeResponseDTO;

import java.util.UUID;

public interface UserHomeQueryService {
    HomeResponseDTO.HomeViewerInfoDTO getHomeViewerInfo();
    HomeResponseDTO.HomeUserInfoDTO getUserProfileInfo(UUID uuid);
}
