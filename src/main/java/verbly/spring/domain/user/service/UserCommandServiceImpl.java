package verbly.spring.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import verbly.spring.domain.user.converter.UserConverter;
import verbly.spring.domain.user.dto.request.UserRequestDTO;
import verbly.spring.domain.user.dto.response.UserResponseDTO;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.enums.UserStatus;
import verbly.spring.domain.user.exception.UserHandler;
import verbly.spring.domain.user.repository.ProfileImageRepository;
import verbly.spring.domain.user.repository.UserRepository;
import verbly.spring.domain.uuid.entity.Uuid;
import verbly.spring.domain.uuid.repository.UuidRepository;
import verbly.spring.global.common.aws.s3.AmazonS3Manager;
import verbly.spring.global.common.code.ErrorStatus;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCommandServiceImpl implements UserCommandService {
    private final UserRepository userRepository;
    private final AmazonS3Manager s3Manager;
    private final UuidRepository uuidRepository;
    private final ProfileImageRepository profileImageRepository;
    private final StringRedisTemplate redisTemplate;
//    private final OnboardingValidator onboardingValidator;

    @Override
    @Transactional // @Transactional에 의해 메서드 종료 시 변경 사항이 DB에 자동 반영
    public User onboardingUser(Long userId, UserRequestDTO.OnboardingDTO request) {
        // 기존 회원이 존재하는지를 따짐
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 온보딩 완료 여부 체크
        if (user.getStatus() != UserStatus.NEED_ONBOARDING) {
            throw new UserHandler(ErrorStatus.ONBOARDING_ALREADY_COMPLETED);
        }

        user.setNativeLang(request.getNativeLang());
        user.setLearningLang(request.getLearningLang());

        user.setStatus(UserStatus.ACTIVE); // 조건이 충족되지 않으면, user의 status는 기본값(NEED_ONBOARDING)인 채로 유지

        return user;
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 멤버 테이블에서 멤버 삭제
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public UserResponseDTO.ProfileUpdateResultDTO updateUser(Long userId, UserRequestDTO.ProfileUpdateDTO request, MultipartFile profileImage) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        if (request.getNickname() == null || request.getNickname().trim().isEmpty()) {
            throw new UserHandler(ErrorStatus.NICKNAME_NOT_EXIST);
        }

//        user.setNickname(request.getNickname());
        user.updateNickname(request.getNickname()); // 도메인 주도 설계(Domain-Driven Design) 원칙에 부합하도록

        if (request.getBio() != null) {
            user.updateBio(request.getBio());
        }

        if (request.getEmail() != null) {
            user.updateEmail(request.getEmail());
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(user.getPhoneNumber())) {
            String verifiedKey = "SMS:VERIFIED:PHONE:" + request.getPhoneNumber();
            String verified = redisTemplate.opsForValue().get(verifiedKey);

            if (verified == null) {
                throw new UserHandler(ErrorStatus.SMS_VERIFICATION_REQUIRED);
            }

            redisTemplate.delete(verifiedKey); // 1회 사용

            user.updatePhoneNumber(request.getPhoneNumber()); // 통과하면 업데이트
        }

        String profileImageUrl = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            profileImageUrl = updateProfileImage(user, profileImage);
        }

        return UserConverter.toProfileUpdateResultDTO(user, profileImageUrl);
    }

    private String updateProfileImage(User user, MultipartFile profileImage) {
        // 파일 형식 검사
        String contentType = profileImage.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new UserHandler(ErrorStatus.INVALID_IMAGE_TYPE);
        }

        // 파일 용량 제한 (10MB)
        long maxFileSize = 10 * 1024 * 1024;
        if (profileImage.getSize() > maxFileSize) {
            throw new UserHandler(ErrorStatus.IMAGE_FILE_TOO_LARGE);
        }

        // UUID 생성 후 저장
        String uuidStr = UUID.randomUUID().toString();
        Uuid uuid = uuidRepository.save(Uuid.builder().uuid(uuidStr).build());

        // S3 업로드
        String profileImageUrl = s3Manager.uploadFile(s3Manager.generateUserKeyName(uuid), profileImage);

        if (user.getProfileImage() != null) {
            // 기존 URL이 S3일 때만 기존 이미지의 키 추출 및 삭제
            String oldKey = s3Manager.extractS3KeyFromUrl(user.getProfileImage().getImageUrl());
            if (oldKey != null) {
                s3Manager.deleteFile(oldKey);
            }

            // update: 기존 엔티티에 새로운 URL만 set
            user.getProfileImage().updateImageUrl(profileImageUrl);
        } else {
            // 새 이미지 insert
            profileImageRepository.save(UserConverter.toProfileImage(profileImageUrl, user));
        }

        return profileImageUrl;
    }
}
