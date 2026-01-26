package verbly.spring.domain.follow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Follow", description = "팔로우/팔로잉 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/following")
public class FollowController {

    private final FollowService followService;

    @Operation(
            summary = "팔로우 생성",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = "특정 사용자를 팔로우합니다.\n\n" +
                    "### PathVariable\n" +
                    "- followeeId: 팔로우 대상 사용자 ID\n\n" +
                    "### 실패 케이스 예시\n" +
                    "- 자기 자신 팔로우 시도\n" +
                    "- 이미 팔로우한 사용자\n" +
                    "- 대상 사용자 없음"
    )
    @Parameters({
            @Parameter(name = "followeeId", description = "팔로우 대상 사용자 ID", example = "1")
    })
    @PostMapping("/{followeeId}")
    public ResponseEntity<ApiResponse<Void>> createFollowing(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long followeeId) {

        Long followerId = customUserDetails.getUser().getId();
        followService.createFollowing(followerId, followeeId);

        return ResponseEntity
                .status(SuccessStatus.FOLLOW_CREATE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.FOLLOW_CREATE_SUCCESS, null));
    }

    @Operation(
            summary = "추천 팔로우 목록 조회",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = "팔로우 추천 사용자 목록을 조회합니다.\n\n" +
                    "### 반환\n" +
                    "- 랜덤 추천 사용자 리스트 (현재 서버 로직: 최대 3명)\n"
    )
    @GetMapping("/recommend")
    public ResponseEntity<ApiResponse<List<UserResponseDTO.FollowRecommendUserResponseDTO>>> getRecommendFollowList(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long followerId = customUserDetails.getUser().getId();
        List<UserResponseDTO.FollowRecommendUserResponseDTO> followRecommendUserResponseDTOList = followService.getRecommendFollowList(followerId);

        return ResponseEntity
                .status(SuccessStatus.FOLLOW_RECOMMEND_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.FOLLOW_RECOMMEND_SUCCESS, followRecommendUserResponseDTOList));
    }

    @Operation(
            summary = "언팔로우",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = "특정 사용자를 언팔로우합니다.\n\n" +
                    "### PathVariable\n" +
                    "- followeeId: 언팔로우 대상 사용자 ID\n\n" +
                    "### 실패 케이스 예시\n" +
                    "- 팔로우 관계가 없는 사용자 언팔로우 시도"
    )
    @Parameters({
            @Parameter(name = "followeeId", description = "언팔로우 대상 사용자 ID", example = "1")
    })
    @DeleteMapping("/{followeeId}")
    public ResponseEntity<ApiResponse<Void>> unfollow(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long followeeId) {

        Long followerId = customUserDetails.getUser().getId();
        followService.unfollow(followerId, followeeId);

        return ResponseEntity
                .status(SuccessStatus.FOLLOW_DELETE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.FOLLOW_DELETE_SUCCESS, null));
    }
}
