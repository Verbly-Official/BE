package verbly.spring.domain.correction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import verbly.spring.domain.correction.entity.Correction;

import java.util.Optional;

public interface CorrectionRepository extends JpaRepository<Correction, Long> {
    long countByPost_Author_Id(Long authorId);
    boolean existsByPostId(Long postId);

    @Query("""
        select c from Correction c
        join fetch c.post p
        left join fetch p.postTags pt
        left join fetch pt.tag t
        where c.id = :correctionId
          and p.author.id = :userId
    """)
    Optional<Correction> findOwnedDetailWithTags(
            @Param("userId") Long userId,
            @Param("correctionId") Long correctionId
    );
}
