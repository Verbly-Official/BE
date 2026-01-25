package verbly.spring.domain.library.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import verbly.spring.domain.library.entity.LibraryItem;
import verbly.spring.domain.library.enums.LibraryItemStatus;
import verbly.spring.domain.library.exception.LibraryErrorStatus;
import verbly.spring.domain.library.exception.LibraryHandler;
import verbly.spring.domain.library.repository.LibraryItemExampleRepository;
import verbly.spring.domain.library.repository.LibraryItemRepository;

@Component
@RequiredArgsConstructor
public class LibraryValidator {

    private final LibraryItemRepository libraryItemRepository;
    private final LibraryItemExampleRepository libraryItemExampleRepository;

    /**
     * 내 아이템인지 + ACTIVE 상태인지 검증
     * (soft delete 이후에도 수정/조회되는 걸 방지)
     */
    public LibraryItem validateOwnedActiveItem(Long userId, Long itemId) {
        LibraryItem item = libraryItemRepository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new LibraryHandler(LibraryErrorStatus.LIBRARY_ITEM_NOT_FOUND));

        if (item.getStatus() != LibraryItemStatus.ACTIVE) {
            throw new LibraryHandler(LibraryErrorStatus.LIBRARY_ITEM_NOT_FOUND);
        }
        return item;
    }

    // 예문이 해당 아이템 소속이 아니라면 예외
    public void validateExampleOwned(Long itemId, Long exampleId) {
        var ex = libraryItemExampleRepository.findById(exampleId)
                .orElseThrow(() -> new LibraryHandler(LibraryErrorStatus.EXAMPLE_NOT_FOUND));

        if (!ex.getLibraryItem().getId().equals(itemId)) {
            throw new LibraryHandler(LibraryErrorStatus.EXAMPLE_NOT_FOUND);
        }
    }
}
