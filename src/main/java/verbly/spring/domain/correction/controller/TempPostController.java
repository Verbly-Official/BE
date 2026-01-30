package verbly.spring.domain.correction.controller;

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

@RestController
@RequestMapping("/api/temp-posts")
@RequiredArgsConstructor
public class TempPostController {
    private final TempPostService tempPostService;
    private final CorrectionService correctionService;

    /**
     * Correction - 글 임시저장
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createTempPost(
            @RequestBody @Valid PostRequestDTO request
    ) {
        Long postId = tempPostService.createTempPost(request);
        return ResponseEntity.status(SuccessStatus.CORRECTION_TEMP_CREATE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CORRECTION_TEMP_CREATE_SUCCESS, postId));
    }

    /**
     * Correction - 임시저장된 글 수정
     */
    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponseDTO.Detail>> updateTempPost(
            @PathVariable Long postId,
            @RequestBody @Valid PostRequestDTO request
    ){
        PostResponseDTO.Detail result = tempPostService.updateTempPost(postId, request);
        return ResponseEntity
                .status(SuccessStatus.CORRECTION_TEMP_UPDATE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CORRECTION_TEMP_UPDATE_SUCCESS, result));
    }

    /**
     * Correction - 임시저장된 글 목록 조회
     */
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
