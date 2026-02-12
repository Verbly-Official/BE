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

@Tag(name = "Correction")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/correction")
public class CorrectionAiAssistController {

    private final CorrectionAiAssistService correctionAiAssistService;

    @Operation(
            summary = "AI 첨삭하기",
            description =
                    "사용자가 'AI 첨삭하기' 버튼을 누르면 호출됩니다.\n\n" +
                            "응답에는 다음이 포함됩니다:\n" +
                            "- Tone & Manner 평가\n" +
                            "- 수정 제안 (최대 3개)\n" +
                            "- 추천 표현 목록\n",
            security = { @SecurityRequirement(name = "JWT TOKEN") },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "AI 첨삭 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "성공 응답 예시",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "CORRECTION2002",
                                                      "message": "Correction - 글을 성공적으로 조회했습니다.",
                                                      "result": {
                                                        "toneManner": {
                                                          "grade": "BAD",
                                                          "casualToFormal": 30,
                                                          "commentKo": "문법 오류가 많고 시제 일치가 되지 않아 의미 전달이 어렵습니다."
                                                        },
                                                        "suggestionCount": 3,
                                                        "suggestions": [
                                                          {
                                                            "original": "Last month I was going to Japan and it was very fun.",
                                                            "revised": "Last month I went to Japan and had a lot of fun.",
                                                            "reasonKo": "과거 시제 'went'와 'had'를 사용하여 경험을 명확히 표현했습니다."
                                                          },
                                                          {
                                                            "original": "We maked a lot of photos and ate many delicious food.",
                                                            "revised": "We took a lot of photos and ate a lot of delicious food.",
                                                            "reasonKo": "'make photos' 대신 'take photos'를 사용하고, 'food'는 불가산 명사이므로 'a lot of'를 사용했습니다."
                                                          },
                                                          {
                                                            "original": "I didn’t knew where is the station, so I asked to a stranger.",
                                                            "revised": "I didn't know where the station was, so I asked a stranger.",
                                                            "reasonKo": "과거 시제 'didn't know'와 간접 의문문 형태 'where the station was'를 사용하고, 'ask to' 대신 'ask'를 사용했습니다."
                                                          }
                                                        ],
                                                        "recommendedPhrases": [
                                                          "Had fun",
                                                          "Took photos",
                                                          "A lot of",
                                                          "Asked stranger",
                                                          "Go hiking"
                                                        ]
                                                      }
                                                    }
                                                """
                                    )
                            )
                    )
            }
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
