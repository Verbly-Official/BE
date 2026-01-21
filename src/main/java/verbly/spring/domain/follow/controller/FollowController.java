package verbly.spring.domain.follow.controller;

import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import verbly.spring.domain.follow.service.FollowService;
import verbly.spring.domain.user.dto.response.UserResponseDTO;
import verbly.spring.global.common.code.SuccessStatus;
import verbly.spring.global.common.response.ApiResponse;
import verbly.spring.global.security.auth.CustomUserDetails;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/following")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{followeeId}")
    public ResponseEntity<ApiResponse<Null>> createFollowing(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long followeeId) {

        Long followerId = customUserDetails.getUser().getId();
        followService.createFollowing(followerId, followeeId);

        return ResponseEntity
                .status(SuccessStatus.FOLLOW_CREATE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.FOLLOW_CREATE_SUCCESS, null));
    }

    @GetMapping("/recommend")
    public ResponseEntity<ApiResponse<List<UserResponseDTO.FollowRecommendUserResponseDTO>>> getRecommendFollowList(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long followerId = customUserDetails.getUser().getId();
        List<UserResponseDTO.FollowRecommendUserResponseDTO> followRecommendUserResponseDTOList = followService.getRecommendFollowList(followerId);

        return ResponseEntity
                .status(SuccessStatus.FOLLOW_RECOMMEND_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.FOLLOW_RECOMMEND_SUCCESS, followRecommendUserResponseDTOList));
    }

    @DeleteMapping("/{followeeId}")
    public ResponseEntity<ApiResponse<Null>> unfollow(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long followeeId) {

        Long followerId = customUserDetails.getUser().getId();
        followService.unfollow(followerId, followeeId);

        return ResponseEntity
                .status(SuccessStatus.FOLLOW_DELETE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.FOLLOW_DELETE_SUCCESS, null));
    }
}
