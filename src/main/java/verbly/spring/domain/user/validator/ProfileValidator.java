package verbly.spring.domain.user.validator;

import org.springframework.stereotype.Component;
import verbly.spring.domain.user.entity.User;

@Component
public class ProfileValidator {
    public boolean hasRequiredProfileInfo(User user) {
        boolean hasNickname = user.getNickname() != null && !user.getNickname().isBlank();

        return hasNickname;
    }
}
