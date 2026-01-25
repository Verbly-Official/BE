package verbly.spring.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import verbly.spring.domain.chat.dto.responseDTO.SearchResponseDTO;

@Service
@RequiredArgsConstructor
public class ChatIntegralService {

    // search chat message
    public SearchResponseDTO getSearchResult(Long participantId, String search) {
    }
}
