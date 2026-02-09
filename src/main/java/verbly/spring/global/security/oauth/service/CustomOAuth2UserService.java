package verbly.spring.global.security.oauth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.user.entity.NotificationSettings;
import verbly.spring.domain.user.entity.ProfileImage;
import verbly.spring.domain.stats.entity.Stats;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.enums.AuthProvider;
import verbly.spring.domain.user.enums.UserStatus;
import verbly.spring.domain.user.exception.UserHandler;
import verbly.spring.domain.user.repository.UserRepository;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.security.auth.CustomOAuth2User;
import verbly.spring.global.security.oauth.userinfo.OAuth2UserInfo;
import verbly.spring.global.security.oauth.userinfo.OAuth2UserInfoFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService { // DefaultOAuth2UserService는 Spring Security에 기본적으로 제공되는 클래스
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest); // 1. 소셜 API에서 사용자 정보 가져오기

        String provider = userRequest.getClientRegistration().getRegistrationId(); // 2. provider 정보 (kakao, google, naver)
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(provider, oAuth2User.getAttributes());
        log.info("🌐 provider: {}, 🐤 attributes: {}, accessToken = {}", provider, oAuth2User.getAttributes(), userRequest.getAccessToken().getTokenValue());

        if (userInfo.getSocialId() == null) {
            throw new UserHandler(ErrorStatus.INVALID_SOCIAL_TOKEN);
        }

        String checkSocialId = provider.toLowerCase() + "_" + userInfo.getSocialId();
        log.info("🔑 최종 socialId: {}", checkSocialId);

        User user = userRepository.findBySocialId(checkSocialId)
                .orElseGet(() -> saveNewUser(userInfo, provider)); // 3. 회원 조회 or 신규 회원 등록

        log.info("🟡 [KAKAO] 로그인 성공");

        Map<String, Object> attributes = oAuth2User.getAttributes();
        log.info("🧩 attributes = {}", attributes);

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        log.info("👤 nickname: {}", profile.get("nickname"));
        log.info("🖼 profile image: {}", profile.get("profile_image_url"));
        log.info("📧 email: {}", kakaoAccount.get("email"));
        log.info("🆔 kakao id: {}", attributes.get("id"));

        return new CustomOAuth2User(user, oAuth2User.getAttributes(), provider); // 4. 반환할 OAuth2User 구현체 (권한 부여용)
//        return super.loadUser(userRequest);
    }

    private User saveNewUser(OAuth2UserInfo userInfo, String provider) {
        log.info("🆕 {} 신규 유저로 저장 시도", provider);

        Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.getAttributes().get("kakao_account");
        //Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        Map<String, Object> profile = (Map<String, Object>) Optional.ofNullable(kakaoAccount.get("profile"))
                .orElse(Collections.emptyMap());
        String socialId = provider.toLowerCase() + "_" + userInfo.getSocialId(); // kakao_12345

        User user = User.builder()
                .socialId(socialId)
                .provider(AuthProvider.from(provider))
                .nickname((String) profile.get("nickname"))
                .email((String) kakaoAccount.get("email"))
                .status(UserStatus.NEED_ONBOARDING)
                .build();

        //String profileImageUrl = (String) profile.get("profile_image_url");
        String profileImageUrl = (String) Optional.ofNullable(profile.get("profile_image_url"))
                .orElse("default_profile_url");

        user.setProfileImage(ProfileImage.builder()
                .user(user)
                .imageUrl(profileImageUrl)
                .build());

        Stats stats = Stats.builder()
                .user(user)
                .userId(user.getId()) // 아직 DB 저장 전이라 null일 수 있음, save 후 update 가능
                .point(100) // 가입 보너스 100P
                .streakDays(0)
                .lastActiveDate(null)
                .lastActiveTime(LocalDateTime.now(ZoneId.of(user.getTimezone())))
                .reviewCount(0L)
                .reviewAverage(0.0)
                .build();
        user.setStats(stats);

        NotificationSettings notificationSettings = NotificationSettings.defaultOf(user);
        user.setNotificationSettings(notificationSettings);

        return userRepository.save(user);
    }
}
