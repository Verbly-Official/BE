package verbly.spring.domain.home.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import verbly.spring.domain.user.enums.Level;

public class HomeResponseDTO {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HomeViewerInfoDTO {
        String imageUrl;
        String nickname;
        String nativeLang;
        Integer following;
        Integer streak;
        Long point;
        Integer correctionReceived;
        Integer correctionGiven;
        Level level;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HomeUserInfoDTO {
        String imageUrl;
        String nickname;
        String nativeLang;
        String description;
        Integer totalPosts;
        Integer follower;
        Integer following;
        Boolean isFollowing;
        Integer correctionReceived;
        Integer correctionGiven;
        Long userId;
    }
}
