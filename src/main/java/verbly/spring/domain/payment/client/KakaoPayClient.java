package verbly.spring.domain.payment.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import verbly.spring.domain.payment.dto.KakaoPayDTO;

@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoPayClient {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String CID = "TCSUBSCRIP";

    @Value("${pay.kakao.admin-key}")
    private String adminKey;

    @Value("${pay.kakao.full-urls.backend.success}")
    private String successUrl;

    @Value("${pay.kakao.full-urls.backend.fail}")
    private String failUrl;

    public KakaoPayDTO.ReadyResponse ready(String orderId, String userId, String itemName, int quantity, Double totalAmount) {
        HttpHeaders headers = getHeaders();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        int newvalue = (int)(totalAmount*1500);

        params.add("cid", CID);
        params.add("partner_order_id", orderId);
        params.add("partner_user_id", userId);
        params.add("item_name", itemName);
        params.add("quantity", String.valueOf(quantity));
        params.add("total_amount", String.valueOf(newvalue));
        params.add("tax_free_amount", "0");

        params.add("approval_url", successUrl);
        params.add("cancel_url", failUrl);
        params.add("fail_url", failUrl);

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

    public KakaoPayDTO.ApproveResponse recurring(String sid, String userId, Double totalAmount) {
        HttpHeaders headers = getHeaders();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        String orderId = "order_" + userId + "_" + System.currentTimeMillis();
        int newvalue = (int)(totalAmount*1500);
        params.add("cid", CID);
        params.add("sid", sid);
        params.add("partner_order_id", orderId);
        params.add("partner_user_id", userId);
        params.add("quantity", "1");
        params.add("total_amount", String.valueOf(newvalue));
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
