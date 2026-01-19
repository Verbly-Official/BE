package verbly.spring.domain.library.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import verbly.spring.domain.library.dto.request.LibraryRequestDTO;
import verbly.spring.domain.library.dto.response.LibraryResponseDTO;

public interface LibraryService {

    LibraryResponseDTO.CreateItemResponse createItem(Long userId, LibraryRequestDTO.CreateItemRequest req);

    Page<LibraryResponseDTO.ItemSummary> listItems(Long userId, String q, Boolean starred, Pageable pageable);

    LibraryResponseDTO.ItemDetail getItemDetail(Long userId, Long itemId);

    void updateItem(Long userId, Long itemId, LibraryRequestDTO.UpdateItemRequest req);

    void deleteItem(Long userId, Long itemId);

    void addExample(Long userId, Long itemId, LibraryRequestDTO.CreateExampleRequest req);

    void deleteExample(Long userId, Long itemId, Long exampleId);
}
