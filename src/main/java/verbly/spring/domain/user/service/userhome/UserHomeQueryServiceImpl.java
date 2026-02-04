package verbly.spring.domain.user.service.userhome;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.follow.repository.FollowRepository;
import verbly.spring.domain.post.enums.PostStatus;
import verbly.spring.domain.post.repository.PostRepository;
import verbly.spring.domain.user.converter.UserConverter;
import verbly.spring.domain.user.dto.response.UserResponseDTO;
import verbly.spring.domain.user.entity.User;
import verbly.spring.global.security.auth.CustomUserDetails;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserHomeQueryServiceImpl implements UserHomeQueryService {

    private final PostRepository postRepository;
    private final FollowRepository followRepository;
    private final UserConverter userConverter;

    @Override
    public UserResponseDTO.HomeViewerInfoDTO getHomeViewerInfo(CustomUserDetails userDetails) {
        User viewer = userDetails != null ? userDetails.getUser() : null;
        long correctionReceived = postRepository.countByAuthorIdAndStatus(viewer.getId(), PostStatus.COMPLETED);
        long following = followRepository.countByFollowerId(viewer.getId());
        return userConverter.toHomeViewerInfoDTO(viewer, following, correctionReceived);
    }
}
