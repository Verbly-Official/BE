package verbly.spring.domain.correction.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import verbly.spring.domain.correction.dto.request.CorrectionEditorRequestDTO;
import verbly.spring.domain.correction.dto.response.CorrectionEditorResponseDTO;
import verbly.spring.domain.correction.dto.response.CorrectionResponseDTO;
import verbly.spring.domain.correction.enums.CorrectorType;
import verbly.spring.domain.correction.service.CorrectionNativeService;
import verbly.spring.global.common.code.SuccessStatus;
import verbly.spring.global.common.response.ApiResponse;

import java.util.List;

@Tag(name = "Correction-Native", description = "외국인(네이티브) 커렉션 요청 리스트 API")
@RestController
@RequestMapping("/api/correction-native")
@RequiredArgsConstructor
public class CorrectionNativeController {
    private final CorrectionNativeService correctionNativeService;

    /**
     * 외국인(네이티브) 유저의 커렉션 요청 리스트
     * - Post.author.learningLang == "en" 인 작성자의 Post에 연결된 Correction 목록 조회
     */
    @Operation(
            summary = "외국인(네이티브) 커렉션 요청 리스트 조회",
            description = "외국인(네이티브) 유저가 첨삭할 커렉션 요청 리스트를 조회합니다.\n\n" +
                    "✅ Paging:\n" +
                    "- page, size 파라미터를 지원합니다.",
            security = { @SecurityRequirement(name = "JWT TOKEN") }
    )
    @Parameters({
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
            @Parameter(name = "size", description = "페이지 크기", example = "10")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CorrectionResponseDTO.MyCorrectionDto>>> getNativeCorrectionRequests(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<CorrectionResponseDTO.MyCorrectionDto> result =
                correctionNativeService.getNativeCorrectionRequests(pageable);

        return ResponseEntity
                .status(SuccessStatus.CORRECTION_READ_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CORRECTION_READ_SUCCESS, result));
    }

    /**
     * 커렉션 문서 상세 조회
     */
    @Operation(
            summary = "커렉션 문서 상세 조회",
            description = "선택한 커렉션 문서의 원문, 교정 단어, 피드백 정보를 조회합니다.",
            security = { @SecurityRequirement(name = "JWT TOKEN") }
    )
    @Parameters({
            @Parameter(name = "correctionId", description = "커렉션 ID", example = "1")
    })
    @GetMapping("/{correctionId}")
    public ResponseEntity<ApiResponse<CorrectionEditorResponseDTO.Detail>> getDetail(
            @PathVariable Long correctionId
    ) {
        CorrectionEditorResponseDTO.Detail result =
                correctionNativeService.getDetail(correctionId);

        return ResponseEntity
                .status(SuccessStatus.CORRECTION_READ_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CORRECTION_READ_SUCCESS, result));
    }

    /**
     * 커렉션 단어(구간) 교정 저장
     */
    @Operation(
            summary = "커렉션 단어 교정 저장",
            description = "문장별 교정 단어(구간)를 저장합니다. 기존 교정은 모두 삭제 후 재저장됩니다.",
            security = { @SecurityRequirement(name = "JWT TOKEN") }
    )
    @Parameters({
            @Parameter(name = "correctionId", description = "커렉션 ID", example = "1")
    })
    @PutMapping("/{correctionId}/words")
    public ResponseEntity<ApiResponse<Void>> upsertWords(
            @PathVariable Long correctionId,
            @RequestBody @Valid CorrectionEditorRequestDTO.UpsertWords request
    ) {
        correctionNativeService.upsertWords(correctionId, request);

        return ResponseEntity
                .status(SuccessStatus.CORRECTION_UPDATE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CORRECTION_UPDATE_SUCCESS, null));
    }

    /**
     * 커렉션 피드백 작성
     */
    @Operation(
            summary = "커렉션 피드백 작성",
            description = "특정 교정 단어에 대한 피드백을 작성합니다.",
            security = { @SecurityRequirement(name = "JWT TOKEN") }
    )
    @Parameters({
            @Parameter(name = "correctionId", description = "커렉션 ID", example = "1")
    })
    @PostMapping("/{correctionId}/feedback")
    public ResponseEntity<ApiResponse<CorrectionEditorResponseDTO.WriteFeedbackResult>> writeFeedback(
            @PathVariable Long correctionId,
            @RequestBody @Valid CorrectionEditorRequestDTO.WriteFeedback request
    ) {
        CorrectionEditorResponseDTO.WriteFeedbackResult result =
                correctionNativeService.writeFeedback(
                        correctionId,
                        CorrectorType.NATIVE_SPEAKER,
                        request
                );

        return ResponseEntity
                .status(SuccessStatus.CORRECTION_CREATE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CORRECTION_CREATE_SUCCESS, result));
    }

    /**
     * 커렉션 피드백 목록 조회
     */
    @Operation(
            summary = "커렉션 피드백 목록 조회",
            description = "해당 커렉션 문서에 달린 모든 피드백을 조회합니다.",
            security = { @SecurityRequirement(name = "JWT TOKEN") }
    )
    @Parameters({
            @Parameter(name = "correctionId", description = "커렉션 ID", example = "1")
    })
    @GetMapping("/{correctionId}/feedback")
    public ResponseEntity<ApiResponse<List<CorrectionEditorResponseDTO.Feedback>>> getFeedback(
            @PathVariable Long correctionId
    ) {
        List<CorrectionEditorResponseDTO.Feedback> result =
                correctionNativeService.getFeedback(correctionId);

        return ResponseEntity
                .status(SuccessStatus.CORRECTION_READ_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CORRECTION_READ_SUCCESS, result));
    }
}
