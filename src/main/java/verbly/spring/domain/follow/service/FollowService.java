package verbly.spring.domain.follow.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import verbly.spring.domain.follow.entity.Follow;
import verbly.spring.domain.follow.exception.FollowHandler;
import verbly.spring.domain.follow.repository.FollowRepository;
import verbly.spring.domain.user.dto.response.UserResponseDTO;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.exception.UserHandler;
import verbly.spring.domain.user.repository.UserRepository;
import verbly.spring.global.common.code.ErrorStatus;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createFollowing(Long followerId, Long followeeId) {

        if(followerId.equals(followeeId))
            throw new FollowHandler(ErrorStatus.CANT_SELF_FOLLOW);

        if(isFollowed(followerId, followeeId)) {
            throw new FollowHandler(ErrorStatus.ALREADY_FOLLOWED);
        }

        Optional<User> optionalFollower = userRepository.findById(followerId);
        if(optionalFollower.isEmpty()) {
            throw new UserHandler(ErrorStatus.USER_NOT_FOUND);
        }
        User follower = optionalFollower.get();

        Optional<User> optionalFollowee = userRepository.findById(followeeId);
        if(optionalFollowee.isEmpty()) {
            throw new UserHandler(ErrorStatus.USER_NOT_FOUND);
        }
        User followee = optionalFollowee.get();

        Follow follow = Follow.of(follower, followee);
        followRepository.save(follow);
    }

    @Transactional
    public List<UserResponseDTO.FollowRecommendUserResponseDTO> getRecommendFollowList(Long followerId) {
        List<User> followRecommendUserList = followRepository.findRandomUser(followerId, PageRequest.of(0, 3));

        return followRecommendUserList
                .stream()
                .map(UserResponseDTO.FollowRecommendUserResponseDTO::from)
                .toList();
    }

    @Transactional
    public void unfollow(Long followerId, Long followeeId) {

        if(!isFollowed(followerId, followeeId))
            throw new FollowHandler(ErrorStatus.NOT_FOLLOWED_USER);

        followRepository.deleteByFollowerIdAndFolloweeId(followerId, followeeId);
    }

    public boolean isFollowed(Long followerId, Long followeeId) {

        return followRepository.existsFollowByFollowerIdAndFolloweeId(followerId, followeeId);
    }
}
