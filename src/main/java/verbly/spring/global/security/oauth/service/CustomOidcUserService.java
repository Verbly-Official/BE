package verbly.spring.global.security.oauth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.enums.AuthProvider;
import verbly.spring.domain.user.enums.UserStatus;
import verbly.spring.domain.user.exception.UserHandler;
import verbly.spring.domain.user.repository.UserRepository;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.security.auth.CustomOAuth2User;
import verbly.spring.global.security.oauth.userinfo.OAuth2UserInfo;
import verbly.spring.global.security.oauth.userinfo.OAuth2UserInfoFactory;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {
    private final UserRepository userRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {

        OidcUser oidcUser = super.loadUser(userRequest); // 1. 소셜 API에서 사용자 정보 가져오기

        String provider = userRequest.getClientRegistration().getRegistrationId(); // 2. provider 정보 (kakao, google, naver)
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(provider, oidcUser.getAttributes());
        log.info("🌐 provider: {}, 🐤 attributes: {}, accessToken = {}", provider, oidcUser.getAttributes(), userRequest.getAccessToken().getTokenValue());

        if (userInfo.getSocialId() == null) {
            throw new UserHandler(ErrorStatus.INVALID_SOCIAL_TOKEN);
        }

        String checkSocialId = provider.toLowerCase() + "_" + userInfo.getSocialId();
        log.info("🔑 최종 socialId: {}", checkSocialId);

        User user = userRepository.findBySocialId(checkSocialId)
                .orElseGet(() -> saveNewUser(userInfo, provider)); // 3. 회원 조회 or 신규 회원 등록

        return new CustomOAuth2User(user, oidcUser.getAttributes()); // 4. 반환할 OAuth2User 구현체 (권한 부여용)
    }

    private User saveNewUser(OAuth2UserInfo userInfo, String provider) {
        log.info("🆕 {} 신규 유저로 저장 시도", provider);
        String socialId = provider.toLowerCase() + "_" + userInfo.getSocialId();
        User user = User.builder()
                .socialId(socialId)
                .provider(AuthProvider.from(provider))
                .status(UserStatus.NEED_ONBOARDING)
                .build();
        return userRepository.save(user);
    }
}
