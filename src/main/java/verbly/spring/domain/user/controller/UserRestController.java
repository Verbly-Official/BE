package verbly.spring.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/user")
public class UserRestController {
//    private final UserQueryService userQueryService;
//    private final UserCommandService userCommandService;
//
//    @PostMapping("/onboarding")
//    @Operation(
//            summary = "회원 초기 정보 등록 (온보딩) API - JWT AccessToken 인증 필요",
//            description = "JWT 인증된 유저가 nativeLang과 learningLang을 등록하는 API입니다.",
//            security = @SecurityRequirement(name = "JWT TOKEN")
//    )
//    public ResponseEntity<ApiResponse<UserResponseDTO.OnboardingResultDTO>> onboard(
//            @AuthenticationPrincipal CustomUserDetails userDetails,
//            @RequestPart("request") @Valid UserRequestDTO.OnboardingDTO request
//    ) {
//        Long userId = userDetails.getUser().getId();
//        User user = userCommandService.onboardingUser(userId, request);
//        return ResponseEntity.status(SuccessStatus.USER_ONBOARDING_SUCCESS.getHttpStatus())
//                .body(ApiResponse.of(SuccessStatus.USER_ONBOARDING_SUCCESS, UserConverter.toOnboardingResponseDTO(user)));
//    }
//
//    @GetMapping("/me")
//    @Operation(summary = "회원 정보 조회 API - JWT AccessToken 인증 필요",
//            description = "JWT 인증된 유저가 자신의 정보를 조회하는 API입니다.",
//            security = { @SecurityRequirement(name = "JWT TOKEN") }
//    )
//    public ResponseEntity<ApiResponse<UserResponseDTO.UserInfoDTO>> getMyInfo(
//            @AuthenticationPrincipal CustomUserDetails userDetails
//    ) {
//        Long userId = userDetails.getUser().getId();
//        UserResponseDTO.UserInfoDTO info = userQueryService.getUserInfo(userId);
//        return ResponseEntity.status(SuccessStatus.USER_INFO_READ_SUCCESS.getHttpStatus())
//                .body(ApiResponse.of(SuccessStatus.USER_INFO_READ_SUCCESS, info));
//    }
//
//    @DeleteMapping
//    @Operation(summary = "회원 탈퇴 API - JWT AccessToken 인증 필요",
//            description = "JWT 인증된 유저가 자신의 계정을 탈퇴(삭제)하는 API입니다.",
//            security = { @SecurityRequirement(name = "JWT TOKEN") }
//    )
//    public ResponseEntity<ApiResponse<Void>> deleteUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
//        userCommandService.deleteUser(userDetails.getUser().getId());
//        return ResponseEntity.status(SuccessStatus.USER_DELETE_SUCCESS.getHttpStatus())
//                .body(ApiResponse.of(SuccessStatus.USER_DELETE_SUCCESS, null));
//    }
//
//    @PatchMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
//    @Operation(
//            summary = "마이페이지 회원 정보 수정 API - JWT AccessToken 인증 필요",
//            description = "JWT 인증된 사용자가 프로필 이미지, 닉네임, bio, 이메일, 전화번호를 수정하는 API입니다.",
//            security = { @SecurityRequirement(name = "JWT TOKEN") }
//    )
//    public ResponseEntity<ApiResponse<UserResponseDTO.ProfileUpdateResultDTO>> updateMyPage(
//            @RequestPart(value = "request") @Valid UserRequestDTO.ProfileUpdateDTO request,
//            @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
//            @AuthenticationPrincipal CustomUserDetails userDetails,
//            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
//    ) {
//        Long userId = userDetails.getUser().getId();
//        UserResponseDTO.ProfileUpdateResultDTO result = userCommandService.updateUser(userId, request, profileImage);
//        return ResponseEntity.status(SuccessStatus.USER_PROFILE_UPDATE_SUCCESS.getHttpStatus())
//                .body(ApiResponse.of(SuccessStatus.USER_PROFILE_UPDATE_SUCCESS, result));
//    }
}
