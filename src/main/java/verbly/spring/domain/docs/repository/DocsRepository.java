package verbly.spring.domain.docs.repository;

import org.springdoc.core.converters.models.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.docs.entity.Document;
import verbly.spring.domain.docs.enums.CorrectorType;

import java.util.List;
import java.util.Optional;

public interface DocsRepository extends JpaRepository<Document, Long> {
    List<Document> findByAuthor_IdAndIsTempFalse(Long authorId, Sort sort);

    List<Document> findByAuthor_IdAndIsTempFalseAndBookmarkedTrue(Long authorId, Sort sort);

    List<Document> findByAuthor_IdAndIsTempFalseAndCorrectorType(Long authorId, CorrectorType correctorType, Sort sort);

    Optional<Document> findByIdAndAuthor_Id(Long id, Long authorId);
}
