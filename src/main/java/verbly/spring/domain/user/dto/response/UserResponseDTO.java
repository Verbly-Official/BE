package verbly.spring.domain.user.dto.response;

import lombok.*;

public class UserResponseDTO {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OnboardingResultDTO {
        private Long userId;
        private String nickname;
        private String profileImage;
        private String learningLang; // 학습 언어
        private String nativeLang; // 모국어
        private String status;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoDTO {
        private Long userId;
        private String nickname;
        private String profileImage;
        private String chatId;

        private String bio;
        private String email;
        private String phoneNumber;

        private long followCount;

        private String learningLang; // 학습 언어
        private String nativeLang; // 모국어

        // 통계
        private int streakDays;
        private long point;
        private int level;

        private long postCount;
        private Long correctionsGiven; // 외국인에게만 뜨는 도움 준 수
        private Long correctionsReceived; // 한국인에게만 뜨는 도움 받은 글

        private String status;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileUpdateResultDTO {
        private Long userId;
        private String profileImage;
        private String nickname;
        private String bio;
        private String email;
        private String phoneNumber;
    }
}
