package verbly.spring.infrastructure.kakao.dto;

import lombok.Data;

public class KakaoPayDTO {
    @Data
    public static class ReadyResponse {
        private String tid;
        private String next_redirect_pc_url; // PC 결제 페이지
        private String next_redirect_mobile_url; // 모바일
    }

    @Data
    public static class ApproveResponse {
        private String aid;
        private String tid;
        private String sid;
        private String partner_order_id;
        private String partner_user_id;
        private Amount amount;
        private String item_name;
    }

    @Data
    public static class Amount {
        private int total;
        private int tax_free;
    }
}
