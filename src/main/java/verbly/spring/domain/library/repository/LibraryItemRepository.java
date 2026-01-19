package verbly.spring.domain.library.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.library.entity.LibraryItem;
import verbly.spring.domain.library.enums.LibraryItemStatus;

import java.util.Optional;

public interface LibraryItemRepository extends JpaRepository<LibraryItem, Long> {

    Optional<LibraryItem> findByIdAndUserId(Long id, Long userId);

    Optional<LibraryItem> findByUserIdAndPhraseNorm(Long userId, String phraseNorm);

//   12개씩 페이징(밑에 애들은 검색옵션별로 정리)
    Page<LibraryItem> findByUserIdAndStatus(Long userId, LibraryItemStatus status, Pageable pageable);

    Page<LibraryItem> findByUserIdAndStatusAndStarred(
            Long userId, LibraryItemStatus status, boolean starred, Pageable pageable
    );

    Page<LibraryItem> findByUserIdAndStatusAndPhraseContaining(
            Long userId, LibraryItemStatus status, String q, Pageable pageable
    );

    Page<LibraryItem> findByUserIdAndStatusAndStarredAndPhraseContaining(
            Long userId, LibraryItemStatus status, boolean starred, String q, Pageable pageable
    );
}
