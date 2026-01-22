package verbly.spring.domain.correction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.correction.converter.CorrectionConverter;
import verbly.spring.domain.correction.dto.request.CorrectionRequestDTO;
import verbly.spring.domain.correction.dto.response.CorrectionResponseDTO;
import verbly.spring.domain.correction.exception.CorrectionHandler;
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

    /**
     * 내 문서 조회
     */
    public List<CorrectionResponseDTO> getMyCorrections() {
        Long userId = SecurityUtils.getCurrentUserId();

        List<Post> posts = postRepository.findAllByAuthorIdOrderByIdDesc(userId);
        return posts.stream()
                .map(CorrectionConverter::toResponseDTO)
                .toList();
    }

    /**
     * 새 글 작성 (첨삭 요청)
     */
    @Transactional
    public CorrectionResponseDTO createCorrection(CorrectionRequestDTO.CreateDto requestDTO) {
        User user = SecurityUtils.getCurrentUser();

        String title = normalize(requestDTO.getTitle());
        String content = normalize(requestDTO.getContent());

        validateRequiredFields(title, content);

        Post post = Post.builder()
                .author(user)
                .status(PostStatus.PENDING)
                .title(title)
                .content(content)
                .isTemp(false)
                .bookmark(false)
                .build();

        Post saved = postRepository.save(post);
        return CorrectionConverter.toResponseDTO(saved);
    }

    /**
     * 문서 수정
     */
    @Transactional
    public CorrectionResponseDTO updateCorrection(Long correctionId, CorrectionRequestDTO.UpdateDto requestDTO) {
        Long userId = SecurityUtils.getCurrentUserId();

        Post post = findOwnedPostOrThrow(userId, correctionId);

        String newTitle = normalize(defaultIfNull(requestDTO.getTitle(), post.getTitle()));
        String newContent = normalize(defaultIfNull(requestDTO.getContent(), post.getContent()));

        validateRequiredFields(newTitle, newContent);

        if (post.isSameContent(newTitle, newContent)) {
            return CorrectionConverter.toResponseDTO(post);
        }

        post.update(newTitle, newContent);

        Post saved = postRepository.save(post);

        return CorrectionConverter.toResponseDTO(saved);
    }

    /**
     * 문서 삭제
     */
    @Transactional
    public void deleteCorrection(Long correctionId) {
        Long userId = SecurityUtils.getCurrentUserId();

        Post post = findOwnedPostOrThrow(userId, correctionId);

        postRepository.delete(post);
    }

    private Post findOwnedPostOrThrow(Long userId, Long postId) {
        return postRepository.findById(postId)
                .filter(p -> p.getAuthor().getId().equals(userId))
                .orElseThrow(() -> new CorrectionHandler(ErrorStatus.CORRECTION_ACCESS_DENIED));
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
}
