package verbly.spring.domain.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.library.entity.LibraryItemSource;

import java.util.List;

public interface LibraryItemSourceRepository extends JpaRepository<LibraryItemSource, Long> {

    List<LibraryItemSource> findByLibraryItem_Id(Long libraryItemId);
//  마찬가지로 소프트 딜리트
    void deleteByLibraryItem_Id(Long libraryItemId);
}
