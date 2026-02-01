package verbly.spring.domain.correction.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import verbly.spring.domain.correction.dto.response.CorrectionResponseDTO;
import verbly.spring.domain.correction.service.CorrectionNativeService;
import verbly.spring.global.common.code.SuccessStatus;
import verbly.spring.global.common.response.ApiResponse;

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
}
