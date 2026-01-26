package verbly.spring.domain.user.service;

import org.springframework.web.multipart.MultipartFile;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.dto.request.UserRequestDTO;
import verbly.spring.domain.user.dto.response.UserResponseDTO;

public interface UserCommandService {
    User onboardingUser(Long userId, UserRequestDTO.OnboardingDTO request);
    void deleteUser(Long userId);
    UserResponseDTO.ProfileUpdateResultDTO updateUser(Long userId, UserRequestDTO.ProfileUpdateDTO request, MultipartFile profileImage);
}
