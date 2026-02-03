package verbly.spring.domain.auth.service;

import jakarta.servlet.http.HttpServletResponse;
import verbly.spring.domain.auth.dto.response.AuthResponseDTO;

public interface AuthCommandService {
    void logout(HttpServletResponse response, Long userId);
    AuthResponseDTO.ReissueTokenResponseDTO reissue(HttpServletResponse response, String refreshToken);
}
