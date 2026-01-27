package verbly.spring.domain.correction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.correction.converter.CorrectionConverter;
import verbly.spring.domain.correction.dto.request.CorrectionRequestDTO;
import verbly.spring.domain.correction.exception.CorrectionHandler;
import verbly.spring.domain.post.converter.PostConverter;
import verbly.spring.domain.post.dto.request.PostRequestDTO;
import verbly.spring.domain.post.dto.response.PostResponseDTO;
import verbly.spring.domain.post.entity.Post;
import verbly.spring.domain.post.repository.PostRepository;
import verbly.spring.domain.user.entity.User;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.security.utils.SecurityUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TempPostService {
    private final PostRepository postRepository;

    /**
     * Correction 글 임시저장
     */
    @Transactional
    public Long createTempPost(PostRequestDTO request) {
        User user = SecurityUtils.getCurrentUser();
        Post post = Post.builder()
                .author(user)
                .status(null)
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
    public PostResponseDTO.Summary updateTempPost(Long postId, PostRequestDTO requestDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        Post post = findOwnedPostOrThrow(userId, postId);

        String newTitle = normalize(defaultIfNull(requestDTO.getTitle(), post.getTitle()));
        String newContent = normalize(defaultIfNull(requestDTO.getContent(), post.getContent()));

        validateRequiredFields(newTitle, newContent);

        if (post.isSameContent(newTitle, newContent)) {
            return PostConverter.toResponseSummaryDTO(post);
        }

        post.update(newTitle, newContent);

        return PostConverter.toResponseSummaryDTO(post);
    }

    /**
     * Correction 임시저장된 글 목록 조회
     */
    @Transactional
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
    @Transactional
    public PostResponseDTO.Detail getMyTempPost(Long postId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Post post = findOwnedPostOrThrow(userId, postId);

        return PostConverter.toResponseDetailDTO(post);
    }

    /**
     * Correction 임시저장된 글 삭제
     */
//    @Transactional
//    public Long deleteMyTempPost(Long postId){
//        Long userId = SecurityUtils.getCurrentUserId();
//        Post post = findOwnedPostOrThrow(userId, postId);
//    }

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
