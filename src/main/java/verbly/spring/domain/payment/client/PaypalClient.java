package verbly.spring.domain.payment.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import verbly.spring.domain.payment.dto.PaypalDTO;
import verbly.spring.domain.payment.exception.PaymentHandler;
import verbly.spring.global.common.code.ErrorStatus;

import java.util.Base64;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaypalClient {

    private final RestTemplate paypalRestTemplate;

    @Value("${paypal.url}") private String baseUrl;
    @Value("${paypal.client-id}") private String clientId;
    @Value("${paypal.client-secret}") private String clientSecret;

    // 1. 액세스 토큰 발급 (API 호출할 때마다 필요)
    private String getAccessToken() {
        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(encodedAuth);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        return paypalRestTemplate.postForObject(
                baseUrl + "/v1/oauth2/token",
                new HttpEntity<>(body, headers),
                PaypalDTO.TokenResponse.class
        ).getAccessToken();
    }

    // 2. 구독 생성 요청 -> 승인 URL(approval_url) 반환
    public String createSubscription(String planId, String returnUrl, String cancelUrl) {
        String accessToken = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        PaypalDTO.CreateSubscriptionRequest request = PaypalDTO.CreateSubscriptionRequest.builder()
                .planId(planId)
                .applicationContext(PaypalDTO.ApplicationContext.builder()
                        .returnUrl(returnUrl)
                        .cancelUrl(cancelUrl)
                        .userAction("SUBSCRIBE_NOW") // 바로 결제창 뜸
                        .build())
                .build();

        PaypalDTO.SubscriptionResponse response = paypalRestTemplate.postForObject(
                baseUrl + "/v1/billing/subscriptions",
                new HttpEntity<>(request, headers),
                PaypalDTO.SubscriptionResponse.class
        );

        if (response == null || response.getLinks() == null) {
            throw new PaymentHandler(ErrorStatus.PAYPAL_SUBSCRIPTION_ERROR);
        }

        return response.getLinks().stream()
                .filter(link -> "approve".equals(link.getRel()))
                .findFirst()
                .orElseThrow(() -> new PaymentHandler(ErrorStatus.PAYPAL_APPROVAL_URL_NOT_FOUND))
                .getHref();
    }

    // 3. 구독 상태 조회 (스케줄러 & 성공 처리용)
    public PaypalDTO.SubscriptionResponse getSubscriptionStatus(String subscriptionId) {
        String accessToken = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        return paypalRestTemplate.exchange(
                baseUrl + "/v1/billing/subscriptions/" + subscriptionId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                PaypalDTO.SubscriptionResponse.class
        ).getBody();
    }
}
