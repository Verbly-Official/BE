package verbly.spring.infrastructure.kakao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import verbly.spring.infrastructure.kakao.dto.KakaoPayDTO;

@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoPayClient {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String CID = "TCSUBSCRIP";

    @Value("${kakao.pay.admin-key}")
    private String adminKey;

    @Value("${app.backend-domain}")
    private String backendDomain;

    public KakaoPayDTO.ReadyResponse ready(String orderId, String userId, String itemName, int quantity, int totalAmount) {
        HttpHeaders headers = getHeaders();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        params.add("cid", CID);
        params.add("partner_order_id", orderId);
        params.add("partner_user_id", userId);
        params.add("item_name", itemName);
        params.add("quantity", String.valueOf(quantity));
        params.add("total_amount", String.valueOf(totalAmount));
        params.add("tax_free_amount", "0");

        params.add("approval_url", backendDomain + "/api/payment/success");
        params.add("cancel_url", backendDomain + "/api/payment/cancel");
        params.add("fail_url", backendDomain + "/api/payment/fail");

        return restTemplate.postForObject(
                "https://kapi.kakao.com/v1/payment/ready",
                new HttpEntity<>(params, headers),
                KakaoPayDTO.ReadyResponse.class
        );
    }

    public KakaoPayDTO.ApproveResponse approve(String tid, String pgToken, String orderId, String userId) {
        HttpHeaders headers = getHeaders();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        params.add("cid", CID);
        params.add("tid", tid);
        params.add("partner_order_id", orderId);
        params.add("partner_user_id", userId);
        params.add("pg_token", pgToken);

        return restTemplate.postForObject(
                "https://kapi.kakao.com/v1/payment/approve",
                new HttpEntity<>(params, headers),
                KakaoPayDTO.ApproveResponse.class
        );
    }

    public KakaoPayDTO.ApproveResponse recurring(String sid, String userId, int totalAmount) {
        HttpHeaders headers = getHeaders();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        String orderId = "order_" + userId + "_" + System.currentTimeMillis();

        params.add("cid", CID);
        params.add("sid", sid);
        params.add("partner_order_id", orderId);
        params.add("partner_user_id", userId);
        params.add("quantity", "1");
        params.add("total_amount", String.valueOf(totalAmount));
        params.add("tax_free_amount", "0");

        return restTemplate.postForObject(
                "https://kapi.kakao.com/v1/payment/subscription",
                new HttpEntity<>(params, headers),
                KakaoPayDTO.ApproveResponse.class
        );
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "KakaoAK " + adminKey);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        return headers;
    }
}
