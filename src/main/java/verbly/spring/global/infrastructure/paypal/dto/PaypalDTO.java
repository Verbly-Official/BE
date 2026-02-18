package verbly.spring.global.infrastructure.paypal.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.List;

public class PaypalDTO {

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @NoArgsConstructor
    public static class TokenResponse {
        @JsonProperty("access_token") private String accessToken;
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Builder
    public static class CreateSubscriptionRequest {
        @JsonProperty("plan_id") private String planId;
        @JsonProperty("application_context") private ApplicationContext applicationContext;
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Builder
    public static class ApplicationContext {
        @JsonProperty("return_url") private String returnUrl;
        @JsonProperty("cancel_url") private String cancelUrl;
        @JsonProperty("user_action") private String userAction;
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @NoArgsConstructor
    @ToString
    public static class SubscriptionResponse {
        private String id;
        private String status;
        @JsonProperty("plan_id")
        private String planId;
        private List<Link> links;
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @NoArgsConstructor
    public static class Link {
        private String href;
        private String rel;
    }

    @Getter
    @NoArgsConstructor
    public static class PaymentCompleteRequestDto {
        @Schema(description = "페이팔 구독 ID (I-로 시작하는 문자열)", example = "I-BW4J5ABCDEFG")
        private String subscriptionId;
        @Schema(description = "사용자가 구매한 플랜 ID (DB PK)", example = "1")
        private Long planId;
    }
}
