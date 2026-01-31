package verbly.spring.domain.library.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.library.converter.LibraryConverter;
import verbly.spring.domain.library.dto.response.LibraryResponseDTO;
import verbly.spring.domain.library.entity.LibraryItem;
import verbly.spring.domain.library.entity.LibraryItemExample;
import verbly.spring.domain.library.entity.LibraryItemSource;
import verbly.spring.domain.library.enums.LibraryItemStatus;
import verbly.spring.domain.library.repository.LibraryItemExampleRepository;
import verbly.spring.domain.library.repository.LibraryItemRepository;
import verbly.spring.domain.library.repository.LibraryItemSourceRepository;
import verbly.spring.domain.library.validator.LibraryValidator;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LibraryQueryServiceImpl implements LibraryQueryService {

    private final LibraryItemRepository libraryItemRepository;
    private final LibraryItemSourceRepository libraryItemSourceRepository;
    private final LibraryItemExampleRepository libraryItemExampleRepository;
    private final LibraryValidator libraryValidator;

    @Override
    public LibraryResponseDTO.PageResult<LibraryResponseDTO.ItemSummary> listItems(
            Long userId,
            String q,
            Boolean starred,
            Pageable pageable
    ) {
        Page<LibraryItem> page;

        // 검색옵션별 분기
        if (q == null || q.isBlank()) {
            if (starred == null) {
                page = libraryItemRepository.findByUserIdAndStatus(userId, LibraryItemStatus.ACTIVE, pageable);
            } else {
                page = libraryItemRepository.findByUserIdAndStatusAndStarred(userId, LibraryItemStatus.ACTIVE, starred, pageable);
            }
        } else {
            if (starred == null) {
                page = libraryItemRepository.findByUserIdAndStatusAndPhraseContaining(userId, LibraryItemStatus.ACTIVE, q, pageable);
            } else {
                page = libraryItemRepository.findByUserIdAndStatusAndStarredAndPhraseContaining(userId, LibraryItemStatus.ACTIVE, starred, q, pageable);
            }
        }

        List<LibraryResponseDTO.ItemSummary> content = page.getContent().stream()
                .map(LibraryConverter::toItemSummary)
                .toList();

        return new LibraryResponseDTO.PageResult<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Override
    public LibraryResponseDTO.ItemDetail getItemDetail(Long userId, Long itemId) {
        LibraryItem item = libraryValidator.validateOwnedActiveItem(userId, itemId);

        List<LibraryItemSource> sources = libraryItemSourceRepository.findByLibraryItem_Id(itemId);
        List<LibraryItemExample> examples = libraryItemExampleRepository.findByLibraryItem_Id(itemId);

        return LibraryConverter.toItemDetail(item, sources, examples);
    }
}
