package verbly.spring.global.security.oauth.userinfo;

import java.util.Map;

public class KakaoUserInfo extends OAuth2UserInfo {
    public KakaoUserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getSocialId() {
        return attributes.get("id").toString();
    }
}
