package verbly.spring.domain.user.service.userhome;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.correction.repository.CorrectionFeedbackRepository;
import verbly.spring.domain.follow.repository.FollowRepository;
import verbly.spring.domain.post.enums.PostStatus;
import verbly.spring.domain.post.repository.PostRepository;
import verbly.spring.domain.stats.service.StatsCommandService;
import verbly.spring.domain.user.converter.UserConverter;
import verbly.spring.domain.user.dto.response.UserResponseDTO;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.exception.UserHandler;
import verbly.spring.domain.user.repository.UserRepository;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.security.auth.CustomUserDetails;
import verbly.spring.global.security.utils.SecurityUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserHomeQueryServiceImpl implements UserHomeQueryService {

    private final PostRepository postRepository;
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final CorrectionFeedbackRepository correctionFeedbackRepository;

    private final StatsCommandService statsCommandService;

    private final UserConverter userConverter;

    @Override
    public UserResponseDTO.HomeViewerInfoDTO getHomeViewerInfo() {
        Long userId = SecurityUtils.getCurrentUserId();
        User viewer = userRepository.findById(userId).orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        long correctionReceived = postRepository.countByAuthorIdAndStatus(viewer.getId(), PostStatus.COMPLETED);
        long following = followRepository.countByFollowerId(viewer.getId());
        return userConverter.toHomeViewerInfoDTO(viewer, following, correctionReceived);
    }

    @Override
    public UserResponseDTO.HomeUserInfoDTO getUserProfileInfo(UUID uuid){
        Long userId = SecurityUtils.getCurrentUserId();
        User viewer = userRepository.findById(userId).orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        User target = userRepository.findByUuid(uuid);
        long following = followRepository.countByFollowerId(target.getId());
        long follower = followRepository.countByFolloweeId(target.getId());
        long totalPosts = postRepository.countByAuthor_Id(target.getId());
        boolean isFollowing = followRepository.existsFollowByFollowerIdAndFolloweeId(viewer.getId(), target.getId());
        long correctionReceived = postRepository.countByAuthorIdAndStatus(viewer.getId(), PostStatus.COMPLETED);
        long correctionGiven = correctionFeedbackRepository.countDistinctCorrectionByCorrectorId(target.getId());
        return userConverter.toHomeUserInfoDTO(target, totalPosts, following, follower, isFollowing, correctionReceived, correctionGiven);
    }
}
