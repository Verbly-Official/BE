package verbly.spring.domain.correction.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import verbly.spring.domain.correction.dto.request.CorrectionRequestDTO;
import verbly.spring.domain.correction.service.TempPostService;
import verbly.spring.global.common.code.SuccessStatus;
import verbly.spring.global.common.response.ApiResponse;

@RestController
@RequestMapping("/api/temp-posts")
@RequiredArgsConstructor
public class TempPostController {
    private final TempPostService tempPostService;

    /**
     * Correction - 글 임시저장
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createTempPost(
            @RequestBody @Valid CorrectionRequestDTO.CreateDTO request
    ) {
        Long postId = tempPostService.createTempPost(request);
        return ResponseEntity.status(SuccessStatus.CORRECTION_TEMP_CREATE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CORRECTION_TEMP_CREATE_SUCCESS, postId));
    }
}
