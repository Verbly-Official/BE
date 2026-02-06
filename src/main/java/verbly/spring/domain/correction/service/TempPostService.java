package verbly.spring.domain.correction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.correction.exception.CorrectionHandler;
import verbly.spring.domain.post.converter.PostConverter;
import verbly.spring.domain.post.dto.request.PostRequestDTO;
import verbly.spring.domain.post.dto.response.PostResponseDTO;
import verbly.spring.domain.post.entity.Post;
import verbly.spring.domain.post.enums.PostStatus;
import verbly.spring.domain.post.repository.PostRepository;
import verbly.spring.domain.user.entity.User;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.security.utils.SecurityUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TempPostService {
    private final PostRepository postRepository;

    /**
     * Correction 글 임시저장
     */
    @Transactional
    public Long createTempPost(PostRequestDTO.tempDto request) {
        User user = SecurityUtils.getCurrentUser();
        Post post = Post.builder()
                .author(user)
                .status(PostStatus.TEMP)
                .title(request.getTitle())
                .content(request.getContent())
                .temp(true)
                .build();

        return postRepository.save(post).getId();
    }

    /**
     * Correction 임시저장된 글 수정
     */
    @Transactional
    public PostResponseDTO.Detail updateTempPost(Long postId, PostRequestDTO.tempDto requestDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        Post post = findOwnedPostOrThrow(userId, postId);

        String newTitle = normalize(defaultIfNull(requestDTO.getTitle(), post.getTitle()));
        String newContent = normalize(defaultIfNull(requestDTO.getContent(), post.getContent()));

        validateRequiredFields(newTitle, newContent);

        if (post.isSameContent(newTitle, newContent)) {
            return PostConverter.toResponseDetailDTO(post);
        }

        post.update(newTitle, newContent);

        return PostConverter.toResponseDetailDTO(post);
    }

    /**
     * Correction 임시저장된 글 목록 조회
     */
    @Transactional(readOnly = true)
    public List<PostResponseDTO.Summary> getAllTempPosts() {
        Long userId = SecurityUtils.getCurrentUserId();
        return postRepository.findAllByAuthorIdAndTempTrueOrderByCreatedAtDesc(userId)
                .stream()
                .map(PostConverter::toResponseSummaryDTO)
                .toList();
    }

    /**
     * Correction 임시저장된 글 상세 조회
     */
    @Transactional(readOnly = true)
    public PostResponseDTO.Detail getMyTempPost(Long postId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Post post = findOwnedPostOrThrow(userId, postId);

        return PostConverter.toResponseDetailDTO(post);
    }

    /**
     * Correction 임시저장된 글 삭제
     */
    @Transactional
    public void deleteMyTempPost(Long postId){
        Long userId = SecurityUtils.getCurrentUserId();
        Post post = findOwnedPostOrThrow(userId, postId);
        postRepository.delete(post);
    }

    private Post findOwnedPostOrThrow(Long userId,Long postId) {
        return postRepository.findByIdAndAuthorIdAndTempTrue(postId, userId)
                .filter(c -> c.getAuthor().getId().equals(userId))
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
