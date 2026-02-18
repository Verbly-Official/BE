package verbly.spring.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.correction.repository.CorrectionFeedbackRepository;
import verbly.spring.domain.correction.repository.CorrectionRepository;
import verbly.spring.domain.follow.repository.FollowRepository;
import verbly.spring.domain.payment.enums.SubscriptionStatus;
import verbly.spring.domain.payment.repository.SubscriptionRepository;
import verbly.spring.domain.post.repository.PostRepository;
import verbly.spring.domain.user.converter.UserConverter;
import verbly.spring.domain.user.dto.response.UserResponseDTO;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.exception.UserHandler;
import verbly.spring.domain.user.repository.UserRepository;
import verbly.spring.domain.user.validator.ProfileValidator;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.security.jwt.JwtTokenProvider;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CorrectionRepository correctionRepository;
    private final CorrectionFeedbackRepository correctionFeedbackRepository;
    private final FollowRepository followRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ProfileValidator profileValidator;

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO.UserInfoDTO getUserInfo(Long userId){
//        Authentication authentication = jwtTokenProvider.extractAuthentication(request); // 토큰을 파싱하고, Authentication 객체를 추출
//        String socialId = authentication.getName(); // 추출해낸 인증 객체(Authentication)을 통해 사용자 정보를 가져온다.

        User user = userRepository.findById(userId) // UserRepository 로부터 사용자 정보를 조회
                .orElseThrow(()-> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        long totalPosts = postRepository.countByAuthor_Id(user.getId());
        long correctionsGiven = correctionFeedbackRepository.countByCorrector_Id(user.getId());
        long correctionsReceived = correctionRepository.countByPost_Author_Id(user.getId());
        long followingCount = followRepository.countByFollowerId(user.getId());
        boolean isSubscribed = subscriptionRepository.existsByUser_IdAndStatusAndNextPaymentDateAfter(userId, SubscriptionStatus.ACTIVE, LocalDateTime.now());

        return UserConverter.toUserInfoDTO(user, totalPosts, correctionsGiven, correctionsReceived, followingCount, isSubscribed); // 정보 조회에 성공하면, 우리가 정의한 Response DTO인 UserInfoDTO 로 반환
    }
}
