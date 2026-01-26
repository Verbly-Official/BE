package verbly.spring.domain.auth.converter;

import verbly.spring.domain.auth.dto.response.AuthResponseDTO;

public class AuthConverter {
    public static AuthResponseDTO.ReissueTokenResponseDTO toReissueTokenResponseDTO(String accessToken) {
        return AuthResponseDTO.ReissueTokenResponseDTO.builder()
                .accessToken(accessToken)
                .build();
    }
}
