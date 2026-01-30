package verbly.spring.domain.review.controller;

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
import verbly.spring.domain.review.dto.request.ReviewRequestDTO;
import verbly.spring.domain.review.dto.response.ReviewResponseDTO;
import verbly.spring.domain.review.service.ReviewCommandService;
import verbly.spring.domain.review.service.ReviewQueryService;
import verbly.spring.global.common.code.SuccessStatus;
import verbly.spring.global.common.response.ApiResponse;
import verbly.spring.global.security.utils.SecurityUtils;

@Tag(name = "Review", description = "Review(Quiz) 탭 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quizzes")
public class ReviewController {

    private final ReviewCommandService reviewCommandService;
    private final ReviewQueryService reviewQueryService;

    /**
     * 리뷰(퀴즈) 시작
     */
    @Operation(
            summary = "리뷰(퀴즈) 세션 시작",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = """
                    사용자의 리뷰(퀴즈) 세션을 시작합니다.

                    ✅ 동작 요약
                    - 라이브러리 ACTIVE 아이템 중 리뷰 대기(PENDING) 항목을 기반으로 세션을 생성합니다.
                    - 진행 중(IN_PROGRESS) 세션이 이미 있으면 재진입 정책에 따라 기존 세션을 자동 종료(quit)하고 새로 시작합니다.

                    ✅ 요청 바디
                    - 현재는 옵션이 없어 빈 JSON({}) 또는 바디 없이 호출 가능합니다.
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = false,
            description = "퀴즈 시작 요청 예시(현재는 비어 있음)",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "QuizStartRequest 예시",
                            value = "{}"
                    )
            )
    )
    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<ReviewResponseDTO.QuizStartResponse>> start(
            @RequestBody(required = false) ReviewRequestDTO.QuizStartRequest request
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        ReviewResponseDTO.QuizStartResponse res = reviewCommandService.startSession(userId);

        return ResponseEntity.status(SuccessStatus.REVIEW_SESSION_START_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.REVIEW_SESSION_START_SUCCESS, res));
    }

    /**
     * 퀴즈 힌트
     */
    @Operation(
            summary = "퀴즈 힌트 사용",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = """
                    특정 문항에서 힌트를 1회 사용합니다.

                    ✅ 요청 바디
                    - 현재는 빈 JSON({}) 또는 바디 없이 호출 가능합니다.
                    """
    )
    @Parameters({
            @Parameter(name = "sessionId", description = "퀴즈 세션 ID", example = "1", required = true),
            @Parameter(name = "questionId", description = "퀴즈 문항 ID", example = "10", required = true)
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = false,
            description = "힌트 요청 예시(현재는 비어 있음)",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "QuizHintRequest 예시",
                            value = "{}"
                    )
            )
    )
    @PostMapping("/sessions/{sessionId}/questions/{questionId}/hint")
    public ResponseEntity<ApiResponse<ReviewResponseDTO.QuizHintResponse>> hint(
            @PathVariable Long sessionId,
            @PathVariable Long questionId,
            @RequestBody(required = false) ReviewRequestDTO.QuizHintRequest request
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        ReviewResponseDTO.QuizHintResponse res = reviewCommandService.useHint(userId, sessionId, questionId);

        return ResponseEntity.status(SuccessStatus.REVIEW_HINT_USE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.REVIEW_HINT_USE_SUCCESS, res));
    }

    /**
     * 답 제출(문항 단위)
     */
    @Operation(
            summary = "퀴즈 답 제출",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = """
                    특정 문항의 답안을 제출합니다.

                    ✅ 요청 바디
                    - userAnswerJson: 프론트에서 보내는 사용자 답안 JSON(필수)
                    - mistakeNote: 오답 노트(선택)

                    ✅ 응답
                    - 정답 여부/정답키/해설을 반환하며,
                      세션이 계속 진행되는 경우 nextQuestion에 다음 문항을 함께 반환합니다.
                    """
    )
    @Parameters({
            @Parameter(name = "sessionId", description = "퀴즈 세션 ID", example = "1", required = true),
            @Parameter(name = "questionId", description = "퀴즈 문항 ID", example = "10", required = true)
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "답 제출 요청 예시",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "QuizAnswerSubmitRequest 예시",
                            value = """
                                    {
                                      "userAnswerJson": {
                                        "answer": "어색한 분위기를 깨다"
                                      },
                                      "mistakeNote": "뜻을 헷갈림"
                                    }
                                    """
                    )
            )
    )
    @PostMapping("/sessions/{sessionId}/questions/{questionId}/answer")
    public ResponseEntity<ApiResponse<ReviewResponseDTO.QuizAnswerSubmitResponse>> submitAnswer(
            @PathVariable Long sessionId,
            @PathVariable Long questionId,
            @Valid @RequestBody ReviewRequestDTO.QuizAnswerSubmitRequest request
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        ReviewResponseDTO.QuizAnswerSubmitResponse res =
                reviewCommandService.submitAnswer(userId, sessionId, questionId, request);

        return ResponseEntity.status(SuccessStatus.REVIEW_ANSWER_SUBMIT_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.REVIEW_ANSWER_SUBMIT_SUCCESS, res));
    }

    /**
     * 퀴즈 중단(처음부터 다시)
     */
    @Operation(
            summary = "퀴즈 세션 중단(quit)",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = """
                    진행 중인 세션을 중단하고, 세션에 연결된 작업(task)들을 PENDING 상태로 롤백합니다.
                    - 정책상 "처음부터 다시"에 해당합니다.
                    """
    )
    @Parameter(name = "sessionId", description = "퀴즈 세션 ID", example = "1", required = true)
    @PostMapping("/sessions/{sessionId}/quit")
    public ResponseEntity<ApiResponse<ReviewResponseDTO.QuizQuitResponse>> quit(
            @PathVariable Long sessionId
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        ReviewResponseDTO.QuizQuitResponse res = reviewCommandService.quit(userId, sessionId);

        return ResponseEntity.status(SuccessStatus.REVIEW_SESSION_QUIT_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.REVIEW_SESSION_QUIT_SUCCESS, res));
    }

    /**
     * 퀴즈 결과 조회
     */
    @Operation(
            summary = "퀴즈 결과 조회",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = """
                    특정 세션의 결과를 조회합니다.
                    - 완료(COMPLETED) 또는 중단(QUIT)된 세션만 결과를 조회할 수 있습니다.
                    - 오답(mistakes)에는 마지막 제출 기준의 사용자 답안과 정답키/해설이 포함됩니다.
                    """
    )
    @Parameter(name = "sessionId", description = "퀴즈 세션 ID", example = "1", required = true)
    @GetMapping("/sessions/{sessionId}/result")
    public ResponseEntity<ApiResponse<ReviewResponseDTO.QuizResultResponse>> result(
            @PathVariable Long sessionId
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        ReviewResponseDTO.QuizResultResponse res = reviewQueryService.getResult(userId, sessionId);

        return ResponseEntity.status(SuccessStatus.REVIEW_RESULT_READ_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.REVIEW_RESULT_READ_SUCCESS, res));
    }

    /**
     * 오답만 재도전
     */
    @Operation(
            summary = "오답만 재도전",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = """
                    특정 세션에서 오답에 해당하는 task만 골라 PENDING으로 되돌린 뒤,
                    오답만으로 새 세션을 시작합니다.
                    """
    )
    @Parameter(name = "sessionId", description = "기존 퀴즈 세션 ID", example = "1", required = true)
    @PostMapping("/sessions/{sessionId}/mistakes/retry")
    public ResponseEntity<ApiResponse<ReviewResponseDTO.QuizStartResponse>> retryMistakes(
            @PathVariable Long sessionId
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        ReviewResponseDTO.QuizStartResponse res = reviewCommandService.retryMistakes(userId, sessionId);

        return ResponseEntity.status(SuccessStatus.REVIEW_MISTAKES_RETRY_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.REVIEW_MISTAKES_RETRY_SUCCESS, res));
    }
}
