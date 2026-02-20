package verbly.spring.domain.home.converter;

import org.springframework.stereotype.Component;
import verbly.spring.domain.home.dto.response.HomeResponseDTO;
import verbly.spring.domain.user.entity.User;

@Component
public class HomeConverter {
    public static HomeResponseDTO.HomeViewerInfoDTO toHomeViewerInfoDTO(User viewer, long following, long correctionReceived, long correctionGiven) {
        return HomeResponseDTO.HomeViewerInfoDTO.builder()
                .imageUrl(viewer.getProfileImage().getImageUrl())
                .nickname(viewer.getNickname())
                .nativeLang(viewer.getNativeLang())
                .following((int)following)
                .streak(viewer.getStats().getStreakDays())
                .point(viewer.getStats().getPoint())
                .correctionReceived((int)correctionReceived)
                .correctionGiven((int)correctionGiven)
                .level(viewer.getStats().getLevel())
                .build();
    }

                                                                    public static HomeResponseDTO.HomeUserInfoDTO toHomeUserInfoDTO(User target, long totalPosts, long following, long follower,
                                                                    boolean isFollowing, long correctionReceived,long correctionGiven) {
        return HomeResponseDTO.HomeUserInfoDTO.builder()
                .userId(target.getId())
                .imageUrl(target.getProfileImage().getImageUrl())
                .nickname(target.getNickname())
                .nativeLang(target.getNativeLang())
                .description(target.getBio())
                .totalPosts((int)totalPosts)
                .follower((int)follower)
                .following((int)following)
                .isFollowing(isFollowing)
                .correctionReceived((int)correctionReceived)
                .correctionGiven((int)correctionGiven)
                .build();
    }
}
