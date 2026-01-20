package verbly.spring.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import verbly.spring.domain.user.converter.UserConverter;
import verbly.spring.domain.user.dto.request.UserRequestDTO;
import verbly.spring.domain.user.dto.response.UserResponseDTO;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.service.UserCommandService;
import verbly.spring.domain.user.service.UserQueryService;
import verbly.spring.global.common.code.SuccessStatus;
import verbly.spring.global.common.response.ApiResponse;
import verbly.spring.global.security.auth.CustomUserDetails;
import verbly.spring.global.security.utils.SecurityUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/user")
public class UserRestController {
    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;

    @PostMapping("/onboarding")
    @Operation(
            summary = "회원 초기 정보 등록 (온보딩) API - JWT AccessToken 인증 필요",
            description = "JWT 인증된 유저가 nativeLang과 learningLang을 등록하는 API입니다.",
            security = @SecurityRequirement(name = "JWT TOKEN")
    )
    public ResponseEntity<ApiResponse<UserResponseDTO.OnboardingResultDTO>> onboard(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("request") @Valid UserRequestDTO.OnboardingDTO request
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userCommandService.onboardingUser(userId, request);
        return ResponseEntity.status(SuccessStatus.USER_ONBOARDING_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.USER_ONBOARDING_SUCCESS, UserConverter.toOnboardingResponseDTO(user)));
    }

    @GetMapping("/me")
    @Operation(summary = "회원 정보 조회 API - JWT AccessToken 인증 필요",
            description = "JWT 인증된 유저가 자신의 정보를 조회하는 API입니다.",
            security = { @SecurityRequirement(name = "JWT TOKEN") }
    )
    public ResponseEntity<ApiResponse<UserResponseDTO.UserInfoDTO>> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        UserResponseDTO.UserInfoDTO info = userQueryService.getUserInfo(userId);
        return ResponseEntity.status(SuccessStatus.USER_INFO_READ_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.USER_INFO_READ_SUCCESS, info));
    }

    @DeleteMapping
    @Operation(summary = "회원 탈퇴 API - JWT AccessToken 인증 필요",
            description = "JWT 인증된 유저가 자신의 계정을 탈퇴(삭제)하는 API입니다.",
            security = { @SecurityRequirement(name = "JWT TOKEN") }
    )
    public ResponseEntity<ApiResponse<Void>> deleteUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = SecurityUtils.getCurrentUserId();
        userCommandService.deleteUser(userId);
        return ResponseEntity.status(SuccessStatus.USER_DELETE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.USER_DELETE_SUCCESS, null));
    }

    @PatchMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(
            summary = "마이페이지 회원 정보 수정 API - JWT AccessToken 인증 필요",
            description = "JWT 인증된 사용자가 프로필 이미지, 닉네임, bio, 이메일, 전화번호를 수정하는 API입니다.",
            security = { @SecurityRequirement(name = "JWT TOKEN") }
    )
    public ResponseEntity<ApiResponse<UserResponseDTO.ProfileUpdateResultDTO>> updateMyPage(
            @RequestPart(value = "request") @Valid UserRequestDTO.ProfileUpdateDTO request,
            @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        UserResponseDTO.ProfileUpdateResultDTO result = userCommandService.updateUser(userId, request, profileImage);
        return ResponseEntity.status(SuccessStatus.USER_PROFILE_UPDATE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.USER_PROFILE_UPDATE_SUCCESS, result));
    }
}
