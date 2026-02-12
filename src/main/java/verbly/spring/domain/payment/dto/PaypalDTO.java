package verbly.spring.domain.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.util.List;

public class PaypalDTO {

    // [1. 토큰 응답]
    @Getter @NoArgsConstructor
    public static class TokenResponse {
        @JsonProperty("access_token") private String accessToken;
    }

    // [2. 구독 생성 요청]
    @Getter @Builder
    public static class CreateSubscriptionRequest {
        @JsonProperty("plan_id") private String planId;
        @JsonProperty("application_context") private ApplicationContext applicationContext;
    }

    @Getter @Builder
    public static class ApplicationContext {
        @JsonProperty("return_url") private String returnUrl;
        @JsonProperty("cancel_url") private String cancelUrl;
        @JsonProperty("user_action") private String userAction; // "SUBSCRIBE_NOW"
    }

    // [3. 구독 응답 (생성 결과 & 상태 조회 공용)]
    @Getter @NoArgsConstructor @ToString
    public static class SubscriptionResponse {
        private String id;     // 구독 ID (I-XXXXXX)
        private String status; // ACTIVE, CANCELLED, SUSPENDED, EXPIRED, APPROVAL_PENDING
        private List<Link> links;
    }

    @Getter @NoArgsConstructor
    public static class Link {
        private String href;
        private String rel; // "approve"
    }
}
