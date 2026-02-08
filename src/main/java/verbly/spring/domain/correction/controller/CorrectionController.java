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
import verbly.spring.domain.correction.dto.response.CorrectionListResponseDTO;
import verbly.spring.domain.correction.dto.response.CorrectionResponseDTO;
import verbly.spring.domain.correction.enums.CorrectorType;
import verbly.spring.domain.correction.service.CorrectionService;
import verbly.spring.domain.post.enums.PostStatus;
import verbly.spring.global.common.code.SuccessStatus;
import verbly.spring.global.common.response.ApiResponse;

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
                    "- tempPostId: 임시저장 글 ID (Long, 선택)\n" +
                    "- tags: 태그 목록 (List<String>, 선택)\n\n" +
                    "- title: 제목 (String, 필수)\n" +
                    "- content: 내용 (String, 필수)\n\n" +
                    "✅ 동작 방식:\n" +
                    "- tempPostId가 없으면: 새 Post 및 Correction 생성\n" +
                    "- tempPostId가 있으면: 임시저장 Post를 Correction 생성(요청)\n\n" +
                    "📎 tags 필드 설명:\n" +
                    "- tags는 선택 값이며, 전달하지 않으면 태그 없이 생성됩니다.\n" +
                    "- 태그는 문자열 배열 형태로 전달합니다.\n" +
                    "- 예: [\"Business_Email\", \"Job_Application\"]"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Correction Write 요청 예시",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "새 글 제출",
                                    value = """
                                        {
                                          "title": "제목",
                                          "content": "내용",
                                          "tags": ["Business_Email", "Job_Application"]
                                        }
                                        """
                            ),
                            @ExampleObject(
                                    name = "임시저장 글 제출",
                                    value = """
                                        {
                                          "tempPostId": 123,
                                          "title": "임시저장했던 제목(수정 가능)",
                                          "content": "임시저장했던 내용(수정 가능)",
                                          "tags": ["Draft", "Email"]
                                        }
                                        """
                            )
                    }
            )

    )
    @PostMapping
    public ResponseEntity<ApiResponse<CorrectionResponseDTO.CreateCorrectionResponseDTO>> createCorrection(
            @RequestBody @Valid CorrectionRequestDTO.CreateDTO request
    ) {
        CorrectionResponseDTO.CreateCorrectionResponseDTO result = correctionService.createCorrection(request);

        return ResponseEntity.status(SuccessStatus.CORRECTION_CREATE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CORRECTION_CREATE_SUCCESS, result));
    }

    /**
     * Correction - 내 문서 조회
     */
    @Operation(
            summary = "커렉션 - 내 문서 목록 조회",
            description = "자신이 작성한 커렉션 글 목록을 조회합니다.\n\n" +
                    "### QueryString\n" +
                    "모든 쿼리 파라미터는 **선택(Optional)**입니다.\n\n" +
                    "미선택시(GET `/api/correction`) 모든 문서가 조회됩니다.\n\n" +
                    "| 쿼리 파라미터 | 종류 | 기능 |\n" +
                    "| --- | --- | --- |\n" +
                    "| bookmark | true | 즐겨찾기 |\n" +
                    "| sort | true | 최근 항목 |\n" +
                    "| status | COMPLETED, IN_PROGRESS, PENDING | 상단 상태 탭 |\n" +
                    "| corrector | AI_ASSISTANT, NATIVE_SPEAKER | corrector 필터 |\n",
            security = { @SecurityRequirement(name = "JWT TOKEN") }
    )
    @Parameters({
            @Parameter(name = "bookmark", description = "즐겨찾기 필터 (true일 때만 필터 적용)", example = "true"),
            @Parameter(name = "sort", description = "최신순 정렬 (true일 때만 필터 적용)", example = "true"),
            @Parameter(name = "status", description = "상태 탭 필터", example = "COMPLETED"),
            @Parameter(name = "correctorType", description = "correctorType 필터", example = "AI_ASSISTANT")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<CorrectionListResponseDTO>> getMyCorrections(
            @RequestParam(required = false) Boolean bookmark,
            @RequestParam(required = false) Boolean sort,
            @RequestParam(required = false) PostStatus status,
            @RequestParam(required = false) CorrectorType correctorType
    ) {
        CorrectionListResponseDTO result =
                correctionService.getMyCorrections(bookmark, sort, status, correctorType);

        return ResponseEntity
                .status(SuccessStatus.CORRECTION_READ_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CORRECTION_READ_SUCCESS, result));
    }

    /**
     * Correction - 내 문서 상세 조회
     */
    @Operation(
            summary = "커렉션 내 문서 상세 조회",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = "자신이 작성한 커렉션 상세 정보를 조회합니다."
    )
    @GetMapping("/{correctionId}")
    public ResponseEntity<ApiResponse<CorrectionResponseDTO.MyCorrectionDto>> getCorrectionDetail(
            @PathVariable Long correctionId
    ){
        CorrectionResponseDTO.MyCorrectionDto result = correctionService.getCorrectionDetail(correctionId);

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
                    "- content: 내용 (String, 선택)\n" +
                    "- tags: 태그 목록 (List<String>, 선택)\n\n" +

                    "📌 tags 필드 동작 방식:\n" +
                    "- tags 필드를 **전송하지 않으면(null)** 기존 태그 유지\n" +
                    "- tags를 **빈 배열([])로 전송**하면 기존 태그 전체 삭제\n" +
                    "- tags에 **문자열 배열을 전송**하면 기존 태그를 해당 값으로 교체\n\n",
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
                                        "content": "수정된 내용",
                                        "tags": ["MODIFY_TAG_1", "MODIFY_TAG_2"]
                                    }
                                    """
                    )
            )
    )
    @PatchMapping("/{correctionId}")
    public ResponseEntity<ApiResponse<CorrectionResponseDTO.MyCorrectionDto>> updateCorrection(
            @PathVariable Long correctionId,
            @RequestBody @Valid CorrectionRequestDTO.UpdateDTO request
    ) {
        CorrectionResponseDTO.MyCorrectionDto result =
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

    /**
     * 즐겨찾기(bookmark) 추가
     */
    @Operation(
            summary = "커렉션 즐겨찾기 추가",
            description = "내가 작성한 커렉션 문서를 즐겨찾기에 추가합니다.",
            security = { @SecurityRequirement(name = "JWT TOKEN") }
    )
    @PatchMapping("/{correctionId}/bookmark")
    public ResponseEntity<ApiResponse<Long>> addBookmark(
            @PathVariable Long correctionId
    ) {
        correctionService.addBookmark(correctionId);
        return ResponseEntity
                .status(SuccessStatus.CORRECTION_BOOKMARK_ADD_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CORRECTION_BOOKMARK_ADD_SUCCESS, correctionId));
    }

    /**
     * 즐겨찾기(bookmark) 삭제
     */
    @Operation(
            summary = "커렉션 즐겨찾기 삭제",
            description = "내가 작성한 커렉션 문서를 즐겨찾기에서 제거합니다.",
            security = { @SecurityRequirement(name = "JWT TOKEN") }
    )
    @DeleteMapping("/{correctionId}/bookmark")
    public ResponseEntity<ApiResponse<Void>> removeBookmark(
            @PathVariable Long correctionId
    ) {
        correctionService.removeBookmark(correctionId);
        return ResponseEntity
                .status(SuccessStatus.CORRECTION_BOOKMARK_REMOVE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CORRECTION_BOOKMARK_REMOVE_SUCCESS, null));
    }

}
