package verbly.spring.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import verbly.spring.domain.post.dto.response.CommentResponseDTO;
import verbly.spring.domain.post.dto.response.PostResponseDTO;
import verbly.spring.domain.post.service.comment.CommentQueryService;
import verbly.spring.domain.post.service.post.PostCommandService;
import verbly.spring.domain.post.service.post.PostQueryService;
import verbly.spring.domain.user.entity.User;
import verbly.spring.global.common.response.ApiResponse;
import verbly.spring.global.security.auth.CustomUserDetails;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostRestController {

    private final PostQueryService postQueryService;
    private final PostCommandService postCommandService;
    private final CommentQueryService commentQueryService;

    @GetMapping()
    @Operation(summary = "홈 화면 포스트 조회", description = "스크롤 페이지를 위한 slice 객체 반환")
    public ApiResponse<Slice<PostResponseDTO.HomePosts>> getHomePosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User viewer = userDetails != null ? userDetails.getUser() : null;
        return ApiResponse.onSuccess(postQueryService.getHomePosts(pageable, viewer));
    }

    @GetMapping("/{uuid}")
    @Operation(summary = "특정 유저 포스트 조회", description = "스크롤 페이지를 위한 slice 객체 반환")
    public ApiResponse<Slice<PostResponseDTO.UserPosts>> getUserPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @PathVariable(name = "uuid") UUID uuid,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User viewer = userDetails != null ? userDetails.getUser() : null;
        return ApiResponse.onSuccess(postQueryService.getUserPosts(pageable,uuid, viewer));
    }

    @PostMapping("/{postId}/like")
    @Operation(summary = "포스트 좋아요 추가")
    public ApiResponse<PostResponseDTO.AddPostLike> addPostLike(
            @PathVariable(name = "postId") Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User viewer = userDetails != null ? userDetails.getUser() : null;
        return ApiResponse.onSuccess(postCommandService.addPostLike(postId, viewer.getId()));
    }

    @DeleteMapping("/{postId}/like")
    @Operation(summary = "포스트 좋아요 삭제")
    public ApiResponse<PostResponseDTO.AddPostLike> deletePostLike(
            @PathVariable(name = "postId") Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User viewer = userDetails != null ? userDetails.getUser() : null;
        return ApiResponse.onSuccess(postCommandService.deletePostLike(postId, viewer.getId()));
    }

    @GetMapping("{postId}/comments")
    @Operation(summary = "특정 포스트의 댓글 조회", description = "스크롤 페이지를 위한 slice 객체 반환")
    public ApiResponse<Slice<CommentResponseDTO.getComment>> getComments(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @PathVariable(name = "postId") Long postId
    ) {
        return ApiResponse.onSuccess(commentQueryService.getComments(pageable, postId));
    }
}
