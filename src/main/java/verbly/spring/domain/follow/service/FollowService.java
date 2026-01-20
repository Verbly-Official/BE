package verbly.spring.domain.follow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import verbly.spring.domain.follow.repo.FollowRepository;

@RequiredArgsConstructor
@Service
public class FollowService {

    private final FollowRepository followRepository;
}
