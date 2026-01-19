package verbly.spring.domain.library.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import verbly.spring.domain.library.entity.LibraryItem;
import verbly.spring.domain.library.exception.LibraryErrorStatus;
import verbly.spring.domain.library.exception.LibraryException;
import verbly.spring.domain.library.repository.LibraryItemExampleRepository;
import verbly.spring.domain.library.repository.LibraryItemRepository;

@Component
@RequiredArgsConstructor
public class LibraryValidator {

    private final LibraryItemRepository libraryItemRepository;
    private final LibraryItemExampleRepository libraryItemExampleRepository;
//  라이브러리의 아이템이 내꺼인경우만 주고 아니면 예외 터트리기!
    public LibraryItem validateOwnedItem(Long userId, Long itemId) {
        return libraryItemRepository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new LibraryException(LibraryErrorStatus.LIBRARY_ITEM_NOT_FOUND));
    }
// 예문이 해당 아이템 소속이 아니라면 예외 터트리기!
    public void validateExampleOwned(Long itemId, Long exampleId) {
        var ex = libraryItemExampleRepository.findById(exampleId)
                .orElseThrow(() -> new LibraryException(LibraryErrorStatus.EXAMPLE_NOT_FOUND));

        if (!ex.getLibraryItem().getId().equals(itemId)) {
            throw new LibraryException(LibraryErrorStatus.EXAMPLE_NOT_FOUND);
        }
    }
}
