package verbly.spring.domain.correction.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import verbly.spring.domain.correction.service.CorrectionService;
import verbly.spring.domain.correction.service.TempPostService;
import verbly.spring.domain.post.dto.request.PostRequestDTO;
import verbly.spring.domain.post.dto.response.PostResponseDTO;
import verbly.spring.global.common.code.SuccessStatus;
import verbly.spring.global.common.response.ApiResponse;

import java.util.List;

@Tag(name = "Correction - Temp Post", description = "Correction 탭 임시저장 API")
@RestController
@RequestMapping("/api/temp-posts")
@RequiredArgsConstructor
public class TempPostController {
    private final TempPostService tempPostService;
    private final CorrectionService correctionService;

    /**
     * Correction - 글 임시저장
     */
    @Operation(
            summary = "커렉션 글 임시저장",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = "새로운 글을 임시저장합니다.\n\n" +
                    "✅ 요청 본문에 포함할 수 있는 값:\n" +
                    "- title: 제목 (String, 필수)\n" +
                    "- content: 내용 (String, 필수)\n"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Correction 임시저장 요청 예시",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "Correction 임시저장 요청 예시",
                                    value = """
                                            {
                                                "title" : "제목",
                                                "content" : "내용"
                                            }
                                            """
                            )
                    }
            )

    )
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createTempPost(
            @RequestBody @Valid PostRequestDTO.tempDto request
    ) {
        Long postId = tempPostService.createTempPost(request);
        return ResponseEntity.status(SuccessStatus.CORRECTION_TEMP_CREATE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CORRECTION_TEMP_CREATE_SUCCESS, postId));
    }

    /**
     * Correction - 임시저장된 글 수정
     */
    @Operation(
            summary = "커렉션 임시저장 글 수정",
            description = "자신의 임시저장 글을 수정합니다.\n\n" +
                    "✅ 수정 가능한 값:\n" +
                    "- title: 제목 (String, 선택)\n" +
                    "- content: 내용 (String, 선택)\n",
            security = { @SecurityRequirement(name = "JWT TOKEN") }
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Correction 임시저장 수정 요청 예시",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Correction 임시저장 수정 요청 예시",
                            value = """
                                    {
                                        "title": "수정된 제목",
                                        "content": "수정된 내용"
                                    }
                                    """
                    )
            )
    )
    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponseDTO.Detail>> updateTempPost(
            @PathVariable Long postId,
            @RequestBody @Valid PostRequestDTO.tempDto request
    ){
        PostResponseDTO.Detail result = tempPostService.updateTempPost(postId, request);
        return ResponseEntity
                .status(SuccessStatus.CORRECTION_TEMP_UPDATE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CORRECTION_TEMP_UPDATE_SUCCESS, result));
    }

    /**
     * Correction - 임시저장된 글 목록 조회
     */
    @Operation(
            summary = "커렉션 - 내 임시저장 문서 목록 조회",
            description = "자신이 임시저장한 커렉션 글 목록을 조회합니다.\n\n",
            security = { @SecurityRequirement(name = "JWT TOKEN") }
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<PostResponseDTO.Summary>>> getAllTempPosts() {
        List<PostResponseDTO.Summary> result =
                tempPostService.getAllTempPosts();

        return ResponseEntity
                .status(SuccessStatus.CORRECTION_TEMP_READ_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CORRECTION_TEMP_READ_SUCCESS, result));
    }

    /**
     * Correction - 임시저장된 글 상세 조회
     */
    @Operation(
            summary = "커렉션 - 내 임시저장 문서 상세 조회",
            description = "자신이 임시저장한 커렉션 글을 조회합니다.\n\n",
            security = { @SecurityRequirement(name = "JWT TOKEN") }
    )
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponseDTO.Detail>> getMyTempPost(
            @PathVariable Long postId
    ) {
        PostResponseDTO.Detail result = tempPostService.getMyTempPost(postId);

        return ResponseEntity
                .status(SuccessStatus.CORRECTION_TEMP_READ_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CORRECTION_TEMP_READ_SUCCESS, result));
    }

    /**
     * Correction - 임시저장된 글 삭제
     */
    @Operation(
            summary = "커렉션 임시저장 글 삭제",
            description = "자신의 커렉션 임시저장 글을 삭제합니다.",
            security = { @SecurityRequirement(name = "JWT TOKEN") }
    )
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deleteTempPost(
            @PathVariable Long postId
    ){
        tempPostService.deleteMyTempPost(postId);
        return ResponseEntity
                .status(SuccessStatus.CORRECTION_TEMP_DELETE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CORRECTION_TEMP_DELETE_SUCCESS, null));
    }
}
