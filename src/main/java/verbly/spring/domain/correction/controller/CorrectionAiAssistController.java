package verbly.spring.domain.correction.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import verbly.spring.domain.correction.dto.response.CorrectionAiAssistResponseDTO;
import verbly.spring.domain.correction.service.CorrectionAiAssistService;
import verbly.spring.global.common.code.SuccessStatus;
import verbly.spring.global.common.response.ApiResponse;

@Tag(name = "Correction-AI", description = "AI 도우미 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/correction")
public class CorrectionAiAssistController {

    private final CorrectionAiAssistService correctionAiAssistService;

    @Operation(
            summary = "AI 첨삭하기 기능입니다.",
            description =
                    "사용자가 'AI 첨삭하기' 버튼을 누르면 호출됩니다.\\n\\n" +
                    "- Tone&Manner\n" +
                    "- 수정 제안(최대 3개)\n" +
                    "- 추천 표현\n\n",
            security = { @SecurityRequirement(name = "JWT TOKEN") }
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "requestBody 없이 호출",
            required = false,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "요청 예시",
                            value = "{}"
                    )
            )
    )
    @Parameters({
            @Parameter(name = "correctionId", description = "커렉션 ID", example = "1")
    })
    @PostMapping("/{correctionId}/ai-assist")
    public ResponseEntity<ApiResponse<CorrectionAiAssistResponseDTO.Result>> getAiHelperPanel(
            @PathVariable Long correctionId
    ) {
        CorrectionAiAssistResponseDTO.Result result =
                correctionAiAssistService.runAiAssist(correctionId);

        return ResponseEntity
                .status(SuccessStatus.CORRECTION_READ_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.CORRECTION_READ_SUCCESS, result));
    }
}
