package verbly.spring.domain.correction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.correction.converter.CorrectionConverter;
import verbly.spring.domain.correction.dto.request.CorrectionRequestDTO;
import verbly.spring.domain.correction.dto.response.CorrectionListResponseDTO;
import verbly.spring.domain.correction.dto.response.CorrectionResponseDTO;
import verbly.spring.domain.correction.entity.Correction;
import verbly.spring.domain.correction.entity.CorrectionBookmark;
import verbly.spring.domain.correction.entity.CorrectionWord;
import verbly.spring.domain.correction.enums.CorrectorType;
import verbly.spring.domain.correction.exception.CorrectionHandler;
import verbly.spring.domain.correction.repository.*;
import verbly.spring.domain.correction.tokenizer.EnglishWordTokenizer;
import verbly.spring.domain.correction.tokenizer.WordToken;
import verbly.spring.domain.post.entity.Post;
import verbly.spring.domain.post.entity.PostTag;
import verbly.spring.domain.post.entity.Tag;
import verbly.spring.domain.post.enums.PostStatus;
import verbly.spring.domain.post.repository.PostRepository;
import verbly.spring.domain.post.repository.PostTagRepository;
import verbly.spring.domain.post.repository.TagRepository;
import verbly.spring.domain.user.entity.User;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.common.utils.RelativeTimeUtils;
import verbly.spring.global.security.utils.SecurityUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CorrectionService {

    private static final int MAX_TAG_COUNT = 10;
    private static final int MAX_TAG_LENGTH = 30;

    private final PostRepository postRepository;
    private final CorrectionRepository correctionRepository;
    private final CorrectionQueryRepository correctionQueryRepository;
    private final CorrectionFeedbackRepository correctionFeedbackRepository;
    private final CorrectionWordRepository correctionWordRepository;
    private final EnglishWordTokenizer englishWordTokenizer;
    private final PostTagRepository postTagRepository;
    private final TagRepository tagRepository;
    private final CorrectionBookmarkRepository correctionBookmarkRepository;

    /**
     * 내 문서 목록 조회
     */
    @Transactional(readOnly = true)
    public CorrectionListResponseDTO getMyCorrections(
            Boolean bookmark,
            Boolean sort,
            PostStatus status,
            CorrectorType correctorType
    ) {
        validateNativeAccess();

        Long userId = SecurityUtils.getCurrentUserId();

        List<CorrectionResponseDTO.MyCorrectionListDto> raw =
                correctionQueryRepository.findMyCorrections(
                        userId,
                        bookmark,
                        sort,
                        status,
                        correctorType
                );

        List<Long> correctionIds = raw.stream()
                .map(CorrectionResponseDTO.MyCorrectionListDto::getCorrectionId)
                .distinct()
                .toList();

        var wordCountMap = correctionWordRepository.countWordsByCorrectionIds(correctionIds).stream()
                .collect(java.util.stream.Collectors.toMap(
                        CorrectionWordRepository.CorrectionCountRow::getCorrectionId,
                        r -> r.getCnt().intValue()
                ));

        List<CorrectionResponseDTO.MyCorrectionListDto> corrections = raw.stream()
                .map(dto -> CorrectionResponseDTO.MyCorrectionListDto.builder()
                        .correctionId(dto.getCorrectionId())
                        .postId(dto.getPostId())
                        .title(dto.getTitle())
                        .status(dto.getStatus())
                        .bookmark(dto.getBookmark())
                        .correctorType(dto.getCorrectorType())
                        .correctorName(dto.getCorrectorName())
                        .correctionCreatedAt(dto.getCorrectionCreatedAt())
                        .correctionUpdatedAt(dto.getCorrectionUpdatedAt())
                        .relativeTime(RelativeTimeUtils.toRelative(dto.getCorrectionCreatedAt()))
                        .wordCount(wordCountMap.getOrDefault(dto.getCorrectionId(), 0))
                        .build()
                )
                .toList();

        long totalRequest =
                correctionQueryRepository.countMyCorrections(
                        userId,
                        bookmark,
                        status,
                        correctorType
                );

        return CorrectionListResponseDTO.builder()
                .totalRequest(totalRequest)
                .corrections(corrections)
                .build();
    }

    /**
     * 문서 상세 조회
     */
    @Transactional(readOnly = true)
    public CorrectionResponseDTO.MyCorrectionDto getCorrectionDetail(Long correctionId){
        validateNativeAccess();

        Long userId = SecurityUtils.getCurrentUserId();

        boolean isBookmarked =
                correctionBookmarkRepository.existsByUserIdAndCorrectionId(userId, correctionId);


        Correction correction = findOwnedCorrectionDetailOrThrow(userId, correctionId);

        var latestFeedback = correctionFeedbackRepository
                .findTopByCorrectionIdOrderByCreatedAtDesc(correctionId)
                .orElse(null);

        CorrectorType correctorType = latestFeedback != null
                ? latestFeedback.getCorrectorType()
                : null;

        String correctorName = null;
        if (latestFeedback != null) {
            if (latestFeedback.getCorrectorType() == CorrectorType.AI_ASSISTANT) {
                correctorName = "AI Assistant";
            } else if (latestFeedback.getCorrectorType() == CorrectorType.NATIVE_SPEAKER
                    && latestFeedback.getCorrector() != null) {
                correctorName = latestFeedback.getCorrector().getNickname();
            }
        }

        return CorrectionConverter.toMyCorrectionDTO(
                correction,
                correctorType,
                correctorName,
                isBookmarked
        );
    }


    /**
     * 새 글 작성 (첨삭 요청)
     * - tempPostId 없으면: 새 Post, Correction 생성
     * - tempPostId 있으면: TEMP Post 제출(PENDING) + Correction 생성
     */
    @Transactional
    public CorrectionResponseDTO.CreateCorrectionResponseDTO createCorrection(CorrectionRequestDTO.CreateDTO requestDTO) {
        validateNativeAccess();

        User user = SecurityUtils.getCurrentUser();

        String title = normalize(requestDTO.getTitle());
        String content = normalize(requestDTO.getContent());

        validateRequiredFields(title, content);

        // 임시저장 글 커렉션 요청할 경우
        if (requestDTO.getTempPostId() != null) {
            Long tempPostId = requestDTO.getTempPostId();

            Post post = findTempPostOrThrow(tempPostId, user);

            post.update(title, content);
            post.changeStatus(PostStatus.PENDING);
            post.changeTemp(false);

            return getCreateCorrectionResponseDTO(requestDTO, post);
        }

        // 새 글 및 커렉션 생성
        Post post = Post.builder()
                .author(user)
                .status(PostStatus.PENDING)
                .title(title)
                .content(content)
                .temp(false)
                .build();

        return getCreateCorrectionResponseDTO(requestDTO, post);
    }

    /**
     * 문서 수정
     */
    @Transactional
    public CorrectionResponseDTO.CreateCorrectionResponseDTO updateCorrection(Long correctionId, CorrectionRequestDTO.UpdateDTO requestDTO) {
        validateNativeAccess();

        Long userId = SecurityUtils.getCurrentUserId();

        Correction correction = findOwnedCorrectionOrThrow(userId, correctionId);
        Post post = correction.getPost();

        String newTitle = normalize(defaultIfNull(requestDTO.getTitle(), post.getTitle()));
        String newContent = normalize(defaultIfNull(requestDTO.getContent(), post.getContent()));

        validateRequiredFields(newTitle, newContent);

        boolean contentChanged = !post.isSameContent(newTitle, newContent);
        boolean tagsRequested = (requestDTO.getTags() != null);

        if (!contentChanged && !tagsRequested) {
            return CorrectionResponseDTO.CreateCorrectionResponseDTO.builder()
                    .correctionId(correction.getId())
                    .postId(post.getId())
                    .build();
        }

        if (contentChanged) {
            post.update(newTitle, newContent);
            postRepository.save(post);

            correctionWordRepository.deleteByCorrectionId(correction.getId());
            saveWords(correction, post.getContent());
        }

        if (tagsRequested) {
            applyTags(post, requestDTO.getTags());
        }

        return CorrectionResponseDTO.CreateCorrectionResponseDTO.builder()
                .correctionId(correction.getId())
                .postId(post.getId())
                .build();
    }

    /**
     * 문서 삭제
     */
    @Transactional
    public void deleteCorrection(Long correctionId) {
        validateNativeAccess();

        Long userId = SecurityUtils.getCurrentUserId();

        Correction correction = findOwnedCorrectionOrThrow(userId, correctionId);
        Post post = correction.getPost();

        correctionBookmarkRepository.deleteAllByCorrection_Id(correctionId);
        correctionFeedbackRepository.deleteAllByCorrectionId(correctionId);
        correctionWordRepository.deleteByCorrectionId(correctionId);
        correctionRepository.delete(correction);
        postRepository.delete(post);
    }

    /**
     * 즐겨찾기(bookmark) 추가
     */
    @Transactional
    public void addBookmark(Long correctionId) {
        validateNativeAccess();

        User user = SecurityUtils.getCurrentUser();
        Long userId = user.getId();

        Correction correction = findOwnedCorrectionOrThrow(userId, correctionId);

        if (correctionBookmarkRepository.existsByUserIdAndCorrectionId(userId, correctionId)) {
            return;
        }

        correctionBookmarkRepository.save(
                CorrectionBookmark.builder()
                        .user(user)
                        .correction(correction)
                        .build()
        );
    }

    /**
     * 즐겨찾기(bookmark) 삭제
     */
    @Transactional
    public void removeBookmark(Long correctionId) {
        validateNativeAccess();

        Long userId = SecurityUtils.getCurrentUserId();

        correctionBookmarkRepository.deleteByUserIdAndCorrectionId(userId, correctionId);
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

    private Correction findOwnedCorrectionDetailOrThrow(Long userId, Long correctionId) {
        return correctionRepository.findOwnedDetailWithTags(userId, correctionId)
                .orElseThrow(() -> new CorrectionHandler(ErrorStatus.CORRECTION_ACCESS_DENIED));
    }


    private Post findTempPostOrThrow(Long tempPostId, User user) {
        Post post = findPostOrThrow(tempPostId);
        validatePostOwner(post, user);
        validateTempPostStatus(post);
        validateNotAlreadySubmitted(post.getId());
        return post;
    }

    private Post findPostOrThrow(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new CorrectionHandler(ErrorStatus.CORRECTION_TEMP_POST_NOT_FOUND));
    }

    private void validatePostOwner(Post post, User user) {
        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new CorrectionHandler(ErrorStatus.CORRECTION_ACCESS_DENIED);
        }
    }

    private void validateTempPostStatus(Post post) {
        if (post.getStatus() != PostStatus.TEMP) {
            throw new CorrectionHandler(ErrorStatus.CORRECTION_TEMP_POST_NOT_FOUND);
        }
    }

    private void validateNotAlreadySubmitted(Long postId) {
        if (correctionRepository.existsByPostId(postId)) {
            throw new CorrectionHandler(ErrorStatus.CORRECTION_TEMP_POST_ALREADY_SUBMITTED);
        }
    }

    private void saveWords(Correction correction, String content) {
        correctionWordRepository.deleteByCorrectionId(correction.getId());

        List<WordToken> tokens = englishWordTokenizer.tokenize(content);

        List<CorrectionWord> words = tokens.stream()
                .map(t -> CorrectionWord.builder()
                        .correction(correction)
                        .sentenceIdx(t.getSentenceIdx())
                        .startIdx(t.getStartIdx())
                        .endIdx(t.getEndIdx())
                        .originalText(t.getText())
                        .correctedText(t.getText())
                        .build()
                )
                .toList();

        if (!words.isEmpty()) {
            correctionWordRepository.saveAll(words);
        }
    }

    // nativeLang == "ko"
    private void validateNativeAccess() {
        User currentUser = SecurityUtils.getCurrentUser();

        if (currentUser == null || !"ko".equalsIgnoreCase(currentUser.getNativeLang())) {
            throw new CorrectionHandler(ErrorStatus.CORRECTION_NATIVE_ACCESS_DENIED);
        }
    }

    private void applyTags(Post post, List<String> rawTags) {
        postTagRepository.deleteByPost(post);
        post.getPostTags().clear();

        if (rawTags == null || rawTags.isEmpty()) return;

        List<String> tags = rawTags.stream()
                .filter(t -> t != null && !t.isBlank())
                .map(String::trim)
                .filter(t -> !t.isBlank())
                .distinct()
                .limit(MAX_TAG_COUNT)
                .toList();

        for (String name : tags) {
            validateTag(name);

            Tag tag = tagRepository.findByName(name)
                    .orElseGet(() -> tagRepository.save(Tag.builder().name(name).build()));

            PostTag postTag = PostTag.builder()
                    .post(post)
                    .tag(tag)
                    .build();

            post.getPostTags().add(postTag);
            postTagRepository.save(postTag);
        }
    }

    private void validateTag(String tag) {
        if (tag == null || tag.isBlank()) {
            throw new CorrectionHandler(ErrorStatus.POST_TAG_NOT_VALIDATE);
        }

        if (tag.matches(".*\\s+.*")) {
            throw new CorrectionHandler(ErrorStatus.POST_TAG_NOT_VALIDATE);
        }

        if (tag.length() > MAX_TAG_LENGTH) {
            throw new CorrectionHandler(ErrorStatus.POST_TAG_NOT_VALIDATE);
        }
    }


    private CorrectionResponseDTO.CreateCorrectionResponseDTO getCreateCorrectionResponseDTO(CorrectionRequestDTO.CreateDTO requestDTO, Post post) {
        Post savedPost = postRepository.save(post);

        applyTags(savedPost, requestDTO.getTags());

        Correction correction = Correction.builder()
                .post(savedPost)
                .build();

        Correction savedCorrection = correctionRepository.save(correction);

        saveWords(savedCorrection, savedPost.getContent());

        return CorrectionConverter.toCreateCorrectionResponse(savedCorrection);
    }

}
