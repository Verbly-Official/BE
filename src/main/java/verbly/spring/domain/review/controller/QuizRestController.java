package verbly.spring.domain.review.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import verbly.spring.domain.review.dto.request.*;
import verbly.spring.domain.review.dto.response.*;
import verbly.spring.domain.review.service.QuizService;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.common.exception.BaseException;
import verbly.spring.global.common.response.ApiResponse;
import verbly.spring.global.security.auth.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quizzes")
public class QuizRestController {

    private final QuizService quizService;

    /** 시큐리티에서 현재 로그인 된 유저 아이디 빼오기 (LibraryRestController 방식 그대로) */
    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            throw new BaseException(ErrorStatus._UNAUTHORIZED);
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof CustomUserDetails cud) {
            return cud.getUserId();
        }

        throw new BaseException(ErrorStatus._UNAUTHORIZED);
    }

    // 리뷰(퀴즈) 시작
    @PostMapping("/sessions")
    public ApiResponse<ReviewResponseDto.QuizStartResponse> start(
            @RequestBody(required = false) ReviewResquestDto.QuizStartRequest request
    ) {
        Long userId = currentUserId();
        ReviewResponseDto.QuizStartResponse res = quizService.startSession(userId);
        return ApiResponse.onSuccess(res);
    }

    // 퀴즈 힌트
    @PostMapping("/sessions/{sessionId}/questions/{questionId}/hint")
    public ApiResponse<ReviewResponseDto.QuizHintResponse> hint(
            @PathVariable Long sessionId,
            @PathVariable Long questionId,
            @RequestBody(required = false) ReviewResquestDto.QuizHintRequest request
    ) {
        Long userId = currentUserId();
        ReviewResponseDto.QuizHintResponse res = quizService.useHint(userId, sessionId, questionId);
        return ApiResponse.onSuccess(res);
    }

    // 답 제출(문항 단위)
    @PostMapping("/sessions/{sessionId}/questions/{questionId}/answer")
    public ApiResponse<ReviewResponseDto.QuizAnswerSubmitResponse> submitAnswer(
            @PathVariable Long sessionId,
            @PathVariable Long questionId,
            @Valid @RequestBody ReviewResquestDto.QuizAnswerSubmitRequest request
    ) {
        Long userId = currentUserId();
        ReviewResponseDto.QuizAnswerSubmitResponse res = quizService.submitAnswer(userId, sessionId, questionId, request);
        return ApiResponse.onSuccess(res);
    }

    // 퀴즈 중단(처음부터 다시)
    @PostMapping("/sessions/{sessionId}/quit")
    public ApiResponse<ReviewResponseDto.QuizQuitResponse> quit(
            @PathVariable Long sessionId
    ) {
        Long userId = currentUserId();
        ReviewResponseDto.QuizQuitResponse res = quizService.quit(userId, sessionId);
        return ApiResponse.onSuccess(res);
    }

    // 퀴즈 결과 조회
    @GetMapping("/sessions/{sessionId}/result")
    public ApiResponse<ReviewResponseDto.QuizResultResponse> result(
            @PathVariable Long sessionId
    ) {
        Long userId = currentUserId();
        ReviewResponseDto.QuizResultResponse res = quizService.getResult(userId, sessionId);
        return ApiResponse.onSuccess(res);
    }

    // 오답만 재도전
    @PostMapping("/sessions/{sessionId}/mistakes/retry")
    public ApiResponse<ReviewResponseDto.QuizStartResponse> retryMistakes(
            @PathVariable Long sessionId
    ) {
        Long userId = currentUserId();
        ReviewResponseDto.QuizStartResponse res = quizService.retryMistakes(userId, sessionId);
        return ApiResponse.onSuccess(res);
    }
}
