package verbly.spring.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import verbly.spring.domain.user.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findBySocialId(String socialId);
    boolean existsByNicknameAndIdNot(String nickname, Long userId);
}
