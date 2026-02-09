package verbly.spring.domain.post.controller.post;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
import verbly.spring.domain.stats.service.StatsCommandService;
import verbly.spring.global.common.response.ApiResponse;
import verbly.spring.global.security.utils.SecurityUtils;

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
    private final StatsCommandService statsCommandService;
    //test
    private final HotPostScheduler scheduler;



    @Override
    @GetMapping()
    public ApiResponse<Slice<PostResponseDTO.HomePosts>> getHomePosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.onSuccess(postQueryService.getHomePosts(pageable, userId));
    }

    @Override
    @GetMapping("/{uuid}")
    public ApiResponse<Slice<PostResponseDTO.UserPosts>> getUserPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @PathVariable(name = "uuid") UUID uuid
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.onSuccess(postQueryService.getUserPosts(pageable,uuid, userId));
    }

    @Override
    @PostMapping("/{postId}/like")
    public ApiResponse<PostResponseDTO.AddPostLike> addPostLike(
            @PathVariable(name = "postId") Long postId
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.onSuccess(postCommandService.addPostLike(postId, userId));
    }

    @Override
    @DeleteMapping("/{postId}/like")
    public ApiResponse<PostResponseDTO.AddPostLike> deletePostLike(
            @PathVariable(name = "postId") Long postId
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.onSuccess(postCommandService.deletePostLike(postId, userId));
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
            @PathVariable(name = "postId") Long postId,
            @RequestBody @Valid CommentRequestDTO.makeComment dto
            ){
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.onSuccess(commentCommandService.getMyComment(userId, postId, dto));
    }

    @Override
    @PostMapping("/home")
    public ApiResponse<PostResponseDTO.HomeWritePost> writeHomePost(
            @RequestBody @Valid PostRequestDTO.HomeWritePost dto
            ){
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.onSuccess(postCommandService.writeHomePost(dto, userId));
    }

    @Override
    @GetMapping("/hot")
    public ApiResponse<List<PostResponseDTO.hotPost>> getHotPosts(){
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.onSuccess(postQueryService.getHotPosts(userId));
    }
}
