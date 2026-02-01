package verbly.spring.domain.library.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import verbly.spring.domain.library.entity.LibraryItem;
import verbly.spring.domain.library.enums.LibraryItemStatus;
import verbly.spring.domain.library.exception.LibraryHandler;
import verbly.spring.domain.library.repository.LibraryItemExampleRepository;
import verbly.spring.domain.library.repository.LibraryItemRepository;
import verbly.spring.global.common.code.ErrorStatus;

@Component
@RequiredArgsConstructor
public class LibraryValidator {

    private final LibraryItemRepository libraryItemRepository;
    private final LibraryItemExampleRepository libraryItemExampleRepository;

    //아이템이 소프트 딜리트 상태인지 확인 함.
    public LibraryItem validateOwnedActiveItem(Long userId, Long itemId) {
        LibraryItem item = libraryItemRepository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new LibraryHandler(ErrorStatus.LIBRARY_ITEM_NOT_FOUND));

        if (item.getStatus() != LibraryItemStatus.ACTIVE) {
            throw new LibraryHandler(ErrorStatus.LIBRARY_ITEM_NOT_FOUND);
        }
        return item;
    }

    // 예문이 해당 아이템 소속이 아니라면 예외
    public void validateExampleOwned(Long itemId, Long exampleId) {
        var ex = libraryItemExampleRepository.findById(exampleId)
                .orElseThrow(() -> new LibraryHandler(ErrorStatus.EXAMPLE_NOT_FOUND));

        if (!ex.getLibraryItem().getId().equals(itemId)) {
            throw new LibraryHandler(ErrorStatus.EXAMPLE_NOT_FOUND);
        }
    }
}
