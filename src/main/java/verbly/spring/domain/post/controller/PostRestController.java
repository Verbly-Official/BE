package verbly.spring.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import verbly.spring.domain.post.dto.response.PostResponseDTO;
import verbly.spring.domain.post.service.PostQueryService;
import verbly.spring.global.common.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostRestController {

    private final PostQueryService postQueryService;

    @GetMapping()
    @Operation(summary = "홈 화면 포스트 조회", description = "스크롤 페이지를 위한 slice 객체 반환")
    public ApiResponse<Slice<PostResponseDTO.HomePosts>> getHomePosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Slice<PostResponseDTO.HomePosts> postSlice = postQueryService.getHomePosts(pageable);
        return ApiResponse.onSuccess(postSlice);
    }
}
