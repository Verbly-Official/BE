package verbly.spring.domain.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.library.entity.LibraryItemExample;

import java.util.List;

public interface LibraryItemExampleRepository extends JpaRepository<LibraryItemExample, Long> {

    List<LibraryItemExample> findByLibraryItem_Id(Long libraryItemId);
    //소프트 딜리트라서 이게 거의 의미없음.
    void deleteByLibraryItem_Id(Long libraryItemId);
}
