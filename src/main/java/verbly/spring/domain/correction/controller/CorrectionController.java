package verbly.spring.domain.correction.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import verbly.spring.domain.correction.dto.request.CorrectionRequestDTO;
import verbly.spring.domain.correction.dto.response.CorrectionResponseDTO;
import verbly.spring.domain.correction.service.CorrectionService;
import verbly.spring.domain.post.enums.PostStatus;
import verbly.spring.global.common.code.SuccessStatus;
import verbly.spring.global.common.response.ApiResponse;

import java.util.List;

@Tag(name = "Correction", description = "Correction 탭 API")
@RestController
@RequestMapping("/api/correction")
@RequiredArgsConstructor
public class CorrectionController {
    private final CorrectionService correctionService;

    /**
     * Correction - Write (새 글 작성)
     */
    @Operation(
            summary = "커렉션 글 작성",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = "새로운 글을 작성합니다.\n\n" +
                    "✅ 요청 본문에 포함할 수 있는 값:\n" +
                    "- title: 제목 (String, 필수)\n" +
                    "- content: 내용 (String, 필수)\n"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Correction Write 요청 예시",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "Correction Write 요청 예시",
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
    public ResponseEntity<ApiResponse<CorrectionResponseDTO>> createCorrection(
            @RequestBody @Valid CorrectionRequestDTO.CreateDto request
    ) {
        CorrectionResponseDTO result = correctionService.createCorrection(request);

        return ResponseEntity.status(SuccessStatus.CORRECTION_CREATE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CORRECTION_CREATE_SUCCESS, result));
    }

    /**
     * Correction - 내 문서 조회
     */
    @Operation(
            summary = "커렉션 - 내 문서 조회",
            description = "자신이 작성한 커렉션 글 목록을 조회합니다.\n\n" +
                    "### QueryString\n" +
                    "모든 쿼리 파라미터는 선택(Optional)입니다.\n\n" +
                    "미선택시(GET `/api/correction`) 모든 문서가 조회됩니다.\n\n" +
                    "| 쿼리 파라미터 | 종류 | 기능 |\n" +
                    "| --- | --- | --- |\n" +
                    "| bookmark | true | 즐겨찾기 |\n" +
                    "| sort | date | 최근 항목 |\n" +
                    "| status | COMPLETED, IN_PROGRESS, PENDING | 상단 상태 탭 |\n" +
                    "| corrector | AI_ASSISTANT, NATIVE_SPEAKER | corrector 필터 |\n",
            security = { @SecurityRequirement(name = "JWT TOKEN") }
    )
    @Parameters({
            @Parameter(name = "bookmark", description = "즐겨찾기 필터 (true일 때만 필터 적용)", example = "true"),
            @Parameter(name = "sort", description = "정렬 기준 (date만 지원)", example = "date"),
            @Parameter(name = "status", description = "상태 탭 필터", example = "COMPLETED"),
            @Parameter(name = "corrector", description = "Corrector 필터", example = "AI_ASSISTANT")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<CorrectionResponseDTO>>> getMyCorrections(
            @RequestParam(required = false) Boolean bookmark,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) PostStatus status,
            @RequestParam(required = false) CorrectorType corrector
    ) {
        List<CorrectionResponseDTO> result = correctionService.getMyCorrections();

        return ResponseEntity
                .status(SuccessStatus.CORRECTION_READ_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CORRECTION_READ_SUCCESS, result));
    }

    /**
     * Correction - 문서 수정
     */
    @Operation(
            summary = "커렉션 글 수정",
            description = "자신의 커렉션 글을 수정합니다.\n\n" +
                    "✅ 수정 가능한 값:\n" +
                    "- title: 제목 (String, 선택)\n" +
                    "- content: 내용 (String, 선택)\n",
            security = { @SecurityRequirement(name = "JWT TOKEN") }
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Correction 수정 요청 예시",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Correction 수정 요청 예시",
                            value = """
                                    {
                                        "title": "수정된 제목",
                                        "content": "수정된 내용"
                                    }
                                    """
                    )
            )
    )
    @PatchMapping("/{correctionId}")
    public ResponseEntity<ApiResponse<CorrectionResponseDTO>> updateCorrection(
            @PathVariable Long correctionId,
            @RequestBody @Valid CorrectionRequestDTO.UpdateDto request
    ) {
        CorrectionResponseDTO result =
                correctionService.updateCorrection(correctionId, request);

        return ResponseEntity
                .status(SuccessStatus.CORRECTION_UPDATE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CORRECTION_UPDATE_SUCCESS, result));
    }

    /**
     * Correction - 문서 삭제
     */
    @Operation(
            summary = "커렉션 글 삭제",
            description = "자신의 커렉션 글을 삭제합니다.",
            security = { @SecurityRequirement(name = "JWT TOKEN") }
    )
    @DeleteMapping("/{correctionId}")
    public ResponseEntity<ApiResponse<Void>> deleteCorrection(
            @PathVariable Long correctionId
    ) {
        correctionService.deleteCorrection(correctionId);

        return ResponseEntity
                .status(SuccessStatus.CORRECTION_DELETE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CORRECTION_DELETE_SUCCESS, null));
    }
}
