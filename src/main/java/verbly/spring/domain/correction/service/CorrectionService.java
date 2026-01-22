package verbly.spring.domain.correction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.correction.converter.CorrectionConverter;
import verbly.spring.domain.correction.dto.request.CorrectionRequestDTO;
import verbly.spring.domain.correction.dto.response.CorrectionResponseDTO;
import verbly.spring.domain.correction.entity.Correction;
import verbly.spring.domain.correction.enums.CorrectorType;
import verbly.spring.domain.correction.exception.CorrectionHandler;
import verbly.spring.domain.correction.repository.CorrectionFeedbackRepository;
import verbly.spring.domain.correction.repository.CorrectionQueryRepository;
import verbly.spring.domain.correction.repository.CorrectionRepository;
import verbly.spring.domain.post.entity.Post;
import verbly.spring.domain.post.enums.PostStatus;
import verbly.spring.domain.post.repository.PostRepository;
import verbly.spring.domain.user.entity.User;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.security.utils.SecurityUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CorrectionService {

    private final PostRepository postRepository;
    private final CorrectionRepository correctionRepository;
    private final CorrectionQueryRepository correctionQueryRepository;
    private final CorrectionFeedbackRepository correctionFeedbackRepository;

    /**
     * 내 문서 조회
     */
    public List<CorrectionResponseDTO.MyCorrectionDto> getMyCorrections(
            Boolean bookmark,
            Boolean sort,
            PostStatus status,
            CorrectorType correctorType
    ) {
        Long userId = SecurityUtils.getCurrentUserId();

        return correctionQueryRepository.findMyCorrections(
                userId,
                bookmark,
                sort,
                status,
                correctorType
        );
    }


    /**
     * 새 글 작성 (첨삭 요청)
     */
    @Transactional
    public CorrectionResponseDTO.CreateCorrectionResponseDTO createCorrection(CorrectionRequestDTO.CreateDTO requestDTO) {
        User user = SecurityUtils.getCurrentUser();

        String title = normalize(requestDTO.getTitle());
        String content = normalize(requestDTO.getContent());

        validateRequiredFields(title, content);

        Post post = Post.builder()
                .author(user)
                .status(PostStatus.PENDING)
                .title(title)
                .content(content)
                .temp(false)
                .bookmark(false)
                .build();

        Post savedPost = postRepository.save(post);

        Correction correction = Correction.builder()
                .post(savedPost)
                .build();

        Correction savedCorrection = correctionRepository.save(correction);

        return CorrectionConverter.toCreateCorrectionResponse(savedCorrection);
    }

    /**
     * 문서 수정
     */
    @Transactional
    public CorrectionResponseDTO.MyCorrectionDto updateCorrection(Long correctionId, CorrectionRequestDTO.UpdateDTO requestDTO) {
        Long userId = SecurityUtils.getCurrentUserId();

        Correction correction = findOwnedCorrectionOrThrow(userId, correctionId);
        Post post = correction.getPost();

        String newTitle = normalize(defaultIfNull(requestDTO.getTitle(), post.getTitle()));
        String newContent = normalize(defaultIfNull(requestDTO.getContent(), post.getContent()));

        validateRequiredFields(newTitle, newContent);

        if (post.isSameContent(newTitle, newContent)) {
            return CorrectionConverter.toMyCorrectionDTO(correction, null, null);
        }

        post.update(newTitle, newContent);
        postRepository.save(post);

        return CorrectionConverter.toMyCorrectionDTO(correction, null, null);
    }

    /**
     * 문서 삭제
     */
    @Transactional
    public void deleteCorrection(Long correctionId) {
        Long userId = SecurityUtils.getCurrentUserId();

        Correction correction = findOwnedCorrectionOrThrow(userId, correctionId);
        Post post = correction.getPost();

        correctionFeedbackRepository.deleteAllByCorrectionId(correctionId);
        correctionRepository.delete(correction);
        postRepository.delete(post);
    }

    private String normalize(String s) {
        return s == null ? null : s.trim();
    }

    private String defaultIfNull(String value, String fallback) {
        return value == null ? fallback : value;
    }

    private void validateRequiredFields(String title, String content) {
        if (title == null || title.isEmpty() || content == null || content.isEmpty()) {
            throw new CorrectionHandler(ErrorStatus.CORRECTION_NOT_VALIDATE);
        }
    }

    private Correction findOwnedCorrectionOrThrow(Long userId, Long correctionId) {
        return correctionRepository.findById(correctionId)
                .filter(c -> c.getPost().getAuthor().getId().equals(userId))
                .orElseThrow(() -> new CorrectionHandler(ErrorStatus.CORRECTION_ACCESS_DENIED));
    }

}
