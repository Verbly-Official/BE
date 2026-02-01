package verbly.spring.domain.library.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.library.converter.LibraryConverter;
import verbly.spring.domain.library.dto.request.LibraryRequestDTO;
import verbly.spring.domain.library.dto.response.LibraryResponseDTO;
import verbly.spring.domain.library.entity.LibraryItem;
import verbly.spring.domain.library.entity.LibraryItemExample;
import verbly.spring.domain.library.exception.LibraryHandler;
import verbly.spring.domain.library.repository.LibraryItemExampleRepository;
import verbly.spring.domain.library.repository.LibraryItemRepository;
import verbly.spring.domain.library.validator.LibraryValidator;
import verbly.spring.global.common.code.ErrorStatus;

@Service
@RequiredArgsConstructor
@Transactional
public class LibraryCommandServiceImpl implements LibraryCommandService {

    private final LibraryItemRepository libraryItemRepository;
    private final LibraryItemExampleRepository libraryItemExampleRepository;
    private final LibraryValidator libraryValidator;

    @Override
    public LibraryResponseDTO.CreateItemResponse createItem(Long userId, LibraryRequestDTO.CreateItemRequest req) {
        String phraseNorm = LibraryConverter.normalizePhrase(req.phrase());

        // 중복 체크
        if (libraryItemRepository.findByUserIdAndPhraseNorm(userId, phraseNorm).isPresent()) {
            throw new LibraryHandler(ErrorStatus.LIBRARY_ITEM_DUPLICATE);
        }

        LibraryItem item = LibraryItem.of(userId, req.phrase(), phraseNorm, req.meaningKo(), req.meaningEn());
        LibraryItem saved = libraryItemRepository.save(item);

        return LibraryConverter.toCreateItemResponse(saved);
    }

    @Override
    public void updateItem(Long userId, Long itemId, LibraryRequestDTO.UpdateItemRequest req) {
        LibraryItem item = libraryValidator.validateOwnedActiveItem(userId, itemId);

        if (req.starred() != null) {
            item.toggleStar(req.starred());
        }


        libraryItemRepository.save(item);
    }

    @Override
    public void deleteItem(Long userId, Long itemId) {
        LibraryItem item = libraryValidator.validateOwnedActiveItem(userId, itemId);
        item.softDelete();
        libraryItemRepository.save(item);
    }

    @Override
    public void addExample(Long userId, Long itemId, LibraryRequestDTO.CreateExampleRequest req) {
        LibraryItem item = libraryValidator.validateOwnedActiveItem(userId, itemId);

        LibraryItemExample ex = LibraryItemExample.of(item, req.exampleEn(), req.exampleKo(), req.source());
        libraryItemExampleRepository.save(ex);
    }

    @Override
    public void deleteExample(Long userId, Long itemId, Long exampleId) {
        libraryValidator.validateOwnedActiveItem(userId, itemId);
        libraryValidator.validateExampleOwned(itemId, exampleId);
        libraryItemExampleRepository.deleteById(exampleId);
    }
}
