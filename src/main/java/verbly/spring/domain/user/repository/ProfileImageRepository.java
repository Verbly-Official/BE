package verbly.spring.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.user.entity.ProfileImage;

import java.util.Optional;

public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {
    Optional<ProfileImage> findByUserId(Long reviewId);
}
