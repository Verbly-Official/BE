package verbly.spring.domain.user.converter;

import verbly.spring.domain.user.dto.response.UserResponseDTO;
import verbly.spring.domain.user.entity.ProfileImage;
import verbly.spring.domain.user.entity.User;

public class UserConverter {
    public static ProfileImage toProfileImage(String imageUrl, User user) {
        return ProfileImage.builder()
                .imageUrl(imageUrl)
                .user(user)
                .build();
    }

    public static UserResponseDTO.OnboardingResultDTO toOnboardingResponseDTO(User user) {
        return UserResponseDTO.OnboardingResultDTO.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage().getImageUrl())
                .learningLang(user.getLearningLang())
                .nativeLang(user.getNativeLang())
                .status(user.getStatus().name())
                .build();
    }

    public static UserResponseDTO.UserInfoDTO toUserInfoDTO(User user, long totalPosts) {
        return UserResponseDTO.UserInfoDTO.builder()
                .userId(user.getId())
                .learningLang(user.getLearningLang())
                .nativeLang(user.getNativeLang())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage().getImageUrl())
                .bio(user.getBio())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .streakDays(user.getStats().getStreakDays())
                .point(user.getStats().getPoint())
                .level(user.getStats().getLevel().getValue())
//                .followCount(user.getFollowCount())
                .totalPosts(totalPosts)
//                .correctionsGiven(correctionsGiven)
//                .correctionsReceived(correctionsReceived)
                .status(user.getStatus().name())
                .build();
    }

    public static UserResponseDTO.ProfileUpdateResultDTO toProfileUpdateResultDTO(User user, String profileImageUrl) {
        return UserResponseDTO.ProfileUpdateResultDTO.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .bio(user.getBio())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .profileImage(
                        profileImageUrl != null
                                ? profileImageUrl
                                : user.getProfileImage() != null
                                ? user.getProfileImage().getImageUrl()
                                : null
                )
                .build();
    }
}
