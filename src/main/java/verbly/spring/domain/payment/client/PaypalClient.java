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

import java.util.Base64;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaypalClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${paypal.url}") private String baseUrl;
    @Value("${paypal.client-id}") private String clientId;
    @Value("${paypal.client-secret}") private String clientSecret;

    /**
     * Obtain an OAuth 2.0 access token from PayPal using the configured client credentials.
     *
     * @return the access token string used to authenticate subsequent PayPal API requests
     */
    private String getAccessToken() {
        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(encodedAuth);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        return restTemplate.postForObject(
                baseUrl + "/v1/oauth2/token",
                new HttpEntity<>(body, headers),
                PaypalDTO.TokenResponse.class
        ).getAccessToken();
    }

    /**
     * Creates a PayPal subscription for the given plan and returns the approval URL where the user can approve the subscription.
     *
     * @param planId    the PayPal plan ID to subscribe to
     * @param returnUrl the URL to which PayPal will redirect after the user approves the subscription
     * @param cancelUrl the URL to which PayPal will redirect if the user cancels the approval flow
     * @return          the approval URL that the caller should redirect the user to
     * @throws RuntimeException if the subscription response does not contain an approval URL
     */
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

        PaypalDTO.SubscriptionResponse response = restTemplate.postForObject(
                baseUrl + "/v1/billing/subscriptions",
                new HttpEntity<>(request, headers),
                PaypalDTO.SubscriptionResponse.class
        );

        return response.getLinks().stream()
                .filter(link -> "approve".equals(link.getRel()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("승인 URL 없음"))
                .getHref();
    }

    /**
     * Retrieve PayPal subscription details for the given subscription ID.
     *
     * @param subscriptionId the PayPal subscription ID to retrieve
     * @return the subscription details as a {@code PaypalDTO.SubscriptionResponse}, or {@code null} if the response body is empty
     */
    public PaypalDTO.SubscriptionResponse getSubscriptionStatus(String subscriptionId) {
        String accessToken = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        return restTemplate.exchange(
                baseUrl + "/v1/billing/subscriptions/" + subscriptionId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                PaypalDTO.SubscriptionResponse.class
        ).getBody();
    }
}