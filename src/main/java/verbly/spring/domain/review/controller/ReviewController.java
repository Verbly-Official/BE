package verbly.spring.domain.review.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/{revieweeId}")
//    @Operation(
//            summary = "회원 초기 정보 등록 (온보딩) API - JWT AccessToken 인증 필요",
//            description = "JWT 인증된 유저가 nativeLang과 learningLang을 등록하는 API입니다.",
//            security = @SecurityRequirement(name = "JWT TOKEN")
//    )
    public ResponseEntity<ApiResponse<Null>> createReview(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long revieweeId, @RequestBody @Valid ReviewRequestDTO reviewRequestDTO) {

        Long reviewerId = customUserDetails.getUser().getId();
        reviewService.createReview(reviewerId, revieweeId, reviewRequestDTO);

        return ResponseEntity
                .status(SuccessStatus.REVIEW_CREATE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.REVIEW_CREATE_SUCCESS, null));
    }

    @GetMapping("/{revieweeId}")
    public ResponseEntity<ApiResponse<List<ReviewResponseDTO>>> getReviewList(@PathVariable Long revieweeId) {

        List<ReviewResponseDTO> reviewResponseDTOList = reviewService.getReviewList(revieweeId);

        return ResponseEntity
                .status(SuccessStatus.REVIEW__READ_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.REVIEW__READ_SUCCESS, reviewResponseDTOList));
    }

    @GetMapping("/{revieweeId}/meta")
    public ResponseEntity<ApiResponse<ReviewMetaResponseDTO>> getReviewMeta(@PathVariable Long revieweeId) {

        ReviewMetaResponseDTO reviewMetaResponseDTO = reviewService.getReviewMeta(revieweeId);

        return ResponseEntity
                .status(SuccessStatus.REVIEW_META_READ_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.REVIEW_META_READ_SUCCESS, reviewMetaResponseDTO));
    }


}
