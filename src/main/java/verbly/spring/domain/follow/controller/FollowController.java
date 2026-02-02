package verbly.spring.domain.follow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Follow", description = "팔로우/팔로잉 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/following")
public class FollowController {

    private final FollowService followService;

    @Operation(
            summary = "팔로우 생성",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description =
                    "특정 사용자를 팔로우합니다.\n\n" +
                    "- 스스로 팔로우 불가" +
                    "- 중복 팔로우 불가"
    )
    @PostMapping("/{followeeId}")
    public ResponseEntity<ApiResponse<Void>> createFollowing(
            @Parameter
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(required = true, name = "followeeId", description = "팔로우 대상 사용자 ID", example = "1")
            @PathVariable Long followeeId) {

        Long followerId = customUserDetails.getUser().getId();
        followService.createFollowing(followerId, followeeId);

        return ResponseEntity
                .status(SuccessStatus.FOLLOW_CREATE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.FOLLOW_CREATE_SUCCESS, null));
    }

    @Operation(
            summary = "추천 팔로우 목록 조회",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description =
                    "팔로우 추천 사용자 목록을 조회합니다.\n\n" +
                    "- 랜덤 추천 사용자 리스트 (3명)\n"
    )
    @GetMapping("/recommend")
    public ResponseEntity<ApiResponse<List<UserResponseDTO.FollowRecommendUserResponseDTO>>> getRecommendFollowList(
            @Parameter
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long followerId = customUserDetails.getUser().getId();
        List<UserResponseDTO.FollowRecommendUserResponseDTO> followRecommendUserResponseDTOList = followService.getRecommendFollowList(followerId);

        return ResponseEntity
                .status(SuccessStatus.FOLLOW_RECOMMEND_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.FOLLOW_RECOMMEND_SUCCESS, followRecommendUserResponseDTOList));
    }

    @Operation(
            summary = "언팔로우(팔로우 취소)",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = "특정 사용자를 언팔로우합니다.\n\n"
    )
    @DeleteMapping("/{followeeId}")
    public ResponseEntity<ApiResponse<Void>> unfollow(
            @Parameter
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(required = true, name = "followeeId", description = "언팔로우 대상 사용자 ID", example = "1")
            @PathVariable Long followeeId) {

        Long followerId = customUserDetails.getUser().getId();
        followService.unfollow(followerId, followeeId);

        return ResponseEntity
                .status(SuccessStatus.FOLLOW_DELETE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.FOLLOW_DELETE_SUCCESS, null));
    }
}
