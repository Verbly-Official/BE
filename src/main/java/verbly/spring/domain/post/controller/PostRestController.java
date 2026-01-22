package verbly.spring.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import verbly.spring.domain.post.dto.response.PostResponseDTO;
import verbly.spring.domain.post.service.PostCommandService;
import verbly.spring.domain.post.service.PostQueryService;
import verbly.spring.domain.user.entity.User;
import verbly.spring.global.common.response.ApiResponse;
import verbly.spring.global.security.auth.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostRestController {

    private final PostQueryService postQueryService;
    private final PostCommandService postCommandService;

    @GetMapping()
    @Operation(summary = "홈 화면 포스트 조회", description = "스크롤 페이지를 위한 slice 객체 반환")
    public ApiResponse<Slice<PostResponseDTO.HomePosts>> getHomePosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User viewer = userDetails != null ? userDetails.getUser() : null;
        Slice<PostResponseDTO.HomePosts> postSlice = postQueryService.getHomePosts(pageable, viewer);
        return ApiResponse.onSuccess(postSlice);
    }

    @PostMapping("/{postId}/like")
    @Operation(summary = "포스트 좋아요 추가")
    public ApiResponse<PostResponseDTO.AddPostLike> addPostLike(
            @PathVariable(name = "postId") Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User viewer = userDetails != null ? userDetails.getUser() : null;
        return ApiResponse.onSuccess(postCommandService.addPostLike(viewer.getId(), postId));
    }

    @DeleteMapping("/{postId}/like")
    @Operation(summary = "포스트 좋아요 삭제")
    public ApiResponse<PostResponseDTO.AddPostLike> deletePostLike(
            @PathVariable(name = "postId") Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User viewer = userDetails != null ? userDetails.getUser() : null;
        return ApiResponse.onSuccess(postCommandService.deletePostLike(viewer.getId(), postId));
    }
}
