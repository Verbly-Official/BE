package verbly.spring.domain.review.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import verbly.spring.domain.review.dto.ReviewMetaResponseDTO;
import verbly.spring.domain.review.dto.ReviewRequestDTO;
import verbly.spring.domain.review.dto.ReviewResponseDTO;
import verbly.spring.domain.review.service.ReviewService;
import verbly.spring.global.common.code.SuccessStatus;
import verbly.spring.global.common.response.ApiResponse;
import verbly.spring.global.security.auth.CustomUserDetails;

import java.util.List;

@Tag(name = "Review", description = "리뷰 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(
            summary = "리뷰 작성",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description =
                    "특정 사용자에 대해 리뷰를 작성합니다.\n\n" +
                    "동일 대상에 대해 리뷰를 여러 번 작성 가능\n" +
                    "Request body:\n" +
                    "- rating: 별점 (Integer, 필수, 1~5)\n" +
                    "- reviewContent: 리뷰 본문 (String, 필수)\n"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Review 작성 Request body",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "리뷰 작성 요청 예시",
                                    value = """
                                            {
                                                "rating" : 3,
                                                "reviewContent" : "리뷰 본문입니다."
                                            }
                                            """
                            )
                    }
            )

    )
    @PostMapping("/{revieweeId}")
    public ResponseEntity<ApiResponse<Void>> createReview(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(required = true, name = "revieweeId", description = "리뷰를 할(리뷰 받을) 대상 사용자 ID", example = "1")
            @PathVariable Long revieweeId, @RequestBody @Valid ReviewRequestDTO reviewRequestDTO) {

        Long reviewerId = customUserDetails.getUser().getId();
        reviewService.createReview(reviewerId, revieweeId, reviewRequestDTO);

        return ResponseEntity
                .status(SuccessStatus.REVIEW_CREATE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.REVIEW_CREATE_SUCCESS, null));
    }

    @Operation(
            summary = "특정 사용자의 전체 리뷰 조회",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = "특정 사용자에 대한 전체 리뷰를 조회합니다.\n\n"
    )
    @GetMapping("/{revieweeId}")
    public ResponseEntity<ApiResponse<List<ReviewResponseDTO>>> getReviewList(
            @Parameter(required = true, name = "revieweeId", description = "리뷰를 조회할 대상 사용자 ID", example = "1")
            @PathVariable Long revieweeId) {

        List<ReviewResponseDTO> reviewResponseDTOList = reviewService.getReviewList(revieweeId);

        return ResponseEntity
                .status(SuccessStatus.REVIEW__READ_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.REVIEW__READ_SUCCESS, reviewResponseDTOList));
    }

    @Operation(
            summary = "특정 사용자의 리뷰 메타 데이터 조회",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description =
                    "특정 사용자에 대한 리뷰 메타 데이터를 조회합니다.\n\n" +
                    "- 메타 데이터: 리뷰 개수, 별점(평점) 평균, 상위 백분율\n" +
                    "- 평균: 소수점 첫째자리까지 표현\n" +
                    "- 상위 백분율: 정수 부분까지 표현\n"
    )
    @GetMapping("/{revieweeId}/meta")
    public ResponseEntity<ApiResponse<ReviewMetaResponseDTO>> getReviewMeta(
            @Parameter(required = true, name = "revieweeId", description = "리뷰 메타 데이터를 조회할 대상 사용자 ID", example = "1")
            @PathVariable Long revieweeId) {

        ReviewMetaResponseDTO reviewMetaResponseDTO = reviewService.getReviewMeta(revieweeId);

        return ResponseEntity
                .status(SuccessStatus.REVIEW_META_READ_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.REVIEW_META_READ_SUCCESS, reviewMetaResponseDTO));
    }


}
