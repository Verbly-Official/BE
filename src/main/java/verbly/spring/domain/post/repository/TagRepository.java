package verbly.spring.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import verbly.spring.domain.post.entity.Tag;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByNameIn(List<String> names);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Tag t SET t.count = t.count + 1 WHERE t.id IN :ids")
    void increaseUsageCount(@Param("ids") List<Long> ids);

    Optional<Tag> findByName(String name);
}
