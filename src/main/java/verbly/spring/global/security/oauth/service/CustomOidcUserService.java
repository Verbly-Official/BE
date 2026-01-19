package verbly.spring.global.security.oauth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.user.entity.ProfileImage;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.enums.AuthProvider;
import verbly.spring.domain.user.enums.UserStatus;
import verbly.spring.domain.user.exception.UserHandler;
import verbly.spring.domain.user.repository.UserRepository;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.security.auth.CustomOAuth2User;
import verbly.spring.global.security.oauth.userinfo.OAuth2UserInfo;
import verbly.spring.global.security.oauth.userinfo.OAuth2UserInfoFactory;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
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

        log.info("🔵 [GOOGLE] 로그인 성공");

        Map<String, Object> attributes = oidcUser.getAttributes();
        log.info("🧩 attributes = {}", attributes);
        if (oidcUser.getIdToken() != null) { // idToken은 null일 수도 있음
            log.info("🧩 idToken claims = {}", oidcUser.getIdToken().getClaims());
        } else {
            log.info("⚠️ idToken is null (userinfo 기반 인증)");
        }

        log.info("👤 name: {}", attributes.get("name"));
        log.info("📛 nickname(given_name): {}", attributes.get("given_name"));
        log.info("🖼 profile image: {}", attributes.get("picture"));
        log.info("📧 email: {}", attributes.get("email"));
        log.info("🆔 sub: {}", attributes.get("sub"));

        return new CustomOAuth2User(user, oidcUser.getAttributes(), provider); // 4. 반환할 OAuth2User 구현체 (권한 부여용)
    }

    private User saveNewUser(OAuth2UserInfo userInfo, String provider) {
        log.info("🆕 {} 신규 유저로 저장 시도", provider);

        String nickname = Optional.ofNullable(userInfo.getAttributes().get("given_name"))
                .map(Object::toString)
                .orElse("익명");
        String email = Optional.ofNullable(userInfo.getAttributes().get("email"))
                .map(Object::toString)
                .orElse("noemail@example.com");
        String socialId = provider.toLowerCase() + "_" + userInfo.getSocialId();

        User user = User.builder()
                .socialId(socialId)
                .provider(AuthProvider.from(provider))
                .nickname(nickname)
                .email(email)
                .status(UserStatus.NEED_ONBOARDING)
                .build();

        String profileImageUrl = Optional.ofNullable(userInfo.getAttributes().get("picture"))
                .map(Object::toString)
                .orElse("default_profile_url");

        user.setProfileImage(ProfileImage.builder()
                .user(user)
                .imageUrl(profileImageUrl)
                .build());

        return userRepository.save(user);
    }
}
