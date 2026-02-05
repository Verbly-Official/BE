package verbly.spring.domain.post.controller.post;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import verbly.spring.domain.post.dto.request.CommentRequestDTO;
import verbly.spring.domain.post.dto.request.PostRequestDTO;
import verbly.spring.domain.post.dto.response.CommentResponseDTO;
import verbly.spring.domain.post.dto.response.PostResponseDTO;
import verbly.spring.domain.post.service.comment.CommentCommandService;
import verbly.spring.domain.post.service.comment.CommentQueryService;
import verbly.spring.domain.post.service.hotpost.HotPostScheduler;
import verbly.spring.domain.post.service.post.PostCommandService;
import verbly.spring.domain.post.service.post.PostQueryService;
import verbly.spring.domain.user.entity.User;
import verbly.spring.global.common.response.ApiResponse;
import verbly.spring.global.security.auth.CustomUserDetails;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostRestController implements PostControllerDocs {

    private final PostQueryService postQueryService;
    private final PostCommandService postCommandService;
    private final CommentQueryService commentQueryService;
    private final CommentCommandService commentCommandService;

    //test
    private final HotPostScheduler scheduler;

    @Override
    @GetMapping()
    public ApiResponse<Slice<PostResponseDTO.HomePosts>> getHomePosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User viewer = userDetails != null ? userDetails.getUser() : null;
        return ApiResponse.onSuccess(postQueryService.getHomePosts(pageable, viewer));
    }

    @Override
    @GetMapping("/{uuid}")
    public ApiResponse<Slice<PostResponseDTO.UserPosts>> getUserPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @PathVariable(name = "uuid") UUID uuid,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User viewer = userDetails != null ? userDetails.getUser() : null;
        return ApiResponse.onSuccess(postQueryService.getUserPosts(pageable,uuid, viewer));
    }

    @Override
    @PostMapping("/{postId}/like")
    public ApiResponse<PostResponseDTO.AddPostLike> addPostLike(
            @PathVariable(name = "postId") Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User viewer = userDetails != null ? userDetails.getUser() : null;
        return ApiResponse.onSuccess(postCommandService.addPostLike(postId, viewer.getId()));
    }

    @Override
    @DeleteMapping("/{postId}/like")
    public ApiResponse<PostResponseDTO.AddPostLike> deletePostLike(
            @PathVariable(name = "postId") Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User viewer = userDetails != null ? userDetails.getUser() : null;
        return ApiResponse.onSuccess(postCommandService.deletePostLike(postId, viewer.getId()));
    }

    @Override
    @GetMapping("{postId}/comments")
    public ApiResponse<Slice<CommentResponseDTO.getComment>> getComments(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @PathVariable(name = "postId") Long postId
    ) {
        return ApiResponse.onSuccess(commentQueryService.getComments(pageable, postId));
    }

    @Override
    @PostMapping("{postId}/comments")
    public ApiResponse<CommentResponseDTO.getMyComment> makeComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable(name = "postId") Long postId,
            @RequestBody @Valid CommentRequestDTO.makeComment dto
            ){
        User viewer = userDetails != null ? userDetails.getUser() : null;
        return ApiResponse.onSuccess(commentCommandService.getMyComment(viewer, postId, dto));
    }

    @Override
    @PostMapping("/home")
    public ApiResponse<PostResponseDTO.HomeWritePost> writeHomePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PostRequestDTO.HomeWritePost dto
            ){
        User viewer = userDetails != null ? userDetails.getUser() : null;
        return ApiResponse.onSuccess(postCommandService.writeHomePost(dto, viewer));
    }

    @Override
    @GetMapping("/hot")
    public ApiResponse<List<PostResponseDTO.hotPost>> getHotPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        return ApiResponse.onSuccess(postQueryService.getHotPosts(userDetails));
    }

    @GetMapping("/test")
    public String triggerBatch() {
        scheduler.runBatch();
        return "배치 실행 완료! DB 확인해보세요.";
    }
}
