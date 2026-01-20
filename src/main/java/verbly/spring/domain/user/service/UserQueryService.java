package verbly.spring.domain.user.service;

import verbly.spring.domain.user.dto.response.UserResponseDTO;

public interface UserQueryService {
    UserResponseDTO.UserInfoDTO getUserInfo(Long userId);
}
