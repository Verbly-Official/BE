package verbly.spring.domain.user.dto.response;

import lombok.*;
import verbly.spring.domain.user.entity.ProfileImage;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.enums.Level;

import java.time.LocalDateTime;

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

        private String bio;
        private String email;
        private String phoneNumber;

        private String learningLang; // 학습 언어
        private String nativeLang; // 모국어

        // 통계
        private int streakDays;
        private long lastActiveTime;
        private long point;
        private int level;

        private long followCount;
        private long totalPosts;
        private long correctionsGiven; // 외국인에게만 뜨는 도움 준 수
        private long correctionsReceived; // 한국인에게만 뜨는 도움 받은 글

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

    @Getter
    @AllArgsConstructor
    @Builder(access = AccessLevel.PRIVATE)
    public static class FollowRecommendUserResponseDTO {
        private Long userId;
        private String nickname;
        private String profileImage;
        private String nativeLang;

        public static FollowRecommendUserResponseDTO from(User user){

            return FollowRecommendUserResponseDTO.builder()
                    .userId(user.getId())
                    .nickname(user.getNickname())
                    .profileImage(user.getProfileImage().getImageUrl())
                    .nativeLang(user.getNativeLang())
                    .build();
        }
    }
}
