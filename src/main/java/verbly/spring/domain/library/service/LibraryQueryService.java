package verbly.spring.domain.library.service;

import org.springframework.data.domain.Pageable;
import verbly.spring.domain.library.dto.response.LibraryResponseDTO;

public interface LibraryQueryService {

    LibraryResponseDTO.PageResult<LibraryResponseDTO.ItemSummary> listItems(Long userId, String q, Boolean starred, Pageable pageable);

    LibraryResponseDTO.ItemDetail getItemDetail(Long userId, Long itemId);
}
