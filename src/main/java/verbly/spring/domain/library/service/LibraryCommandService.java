package verbly.spring.domain.library.service;

import verbly.spring.domain.library.dto.request.LibraryRequestDTO;
import verbly.spring.domain.library.dto.response.LibraryResponseDTO;

public interface LibraryCommandService {

    LibraryResponseDTO.CreateItemResponse createItem(Long userId, LibraryRequestDTO.CreateItemRequest req);

    void updateItem(Long userId, Long itemId, LibraryRequestDTO.UpdateItemRequest req);

    void deleteItem(Long userId, Long itemId);

    void addExample(Long userId, Long itemId, LibraryRequestDTO.CreateExampleRequest req);

    void deleteExample(Long userId, Long itemId, Long exampleId);
}
