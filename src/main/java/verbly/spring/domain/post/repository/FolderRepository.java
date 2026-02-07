package verbly.spring.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.post.entity.Folder;

import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findAllByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    Optional<Folder> findByIdAndOwnerId(Long id, Long ownerId);

    boolean existsByOwnerIdAndName(Long ownerId, String name);
}
