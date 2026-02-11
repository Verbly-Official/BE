package verbly.spring.domain.correction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.correction.converter.CorrectionEditorConverter;
import verbly.spring.domain.correction.dto.request.CorrectionEditorRequestDTO;
import verbly.spring.domain.correction.dto.response.CorrectionEditorQueryDTO;
import verbly.spring.domain.correction.dto.response.CorrectionEditorResponseDTO;
import verbly.spring.domain.correction.dto.response.CorrectionResponseDTO;
import verbly.spring.domain.correction.entity.Correction;
import verbly.spring.domain.correction.entity.CorrectionBookmark;
import verbly.spring.domain.correction.entity.CorrectionFeedback;
import verbly.spring.domain.correction.entity.CorrectionWord;
import verbly.spring.domain.correction.enums.CorrectorType;
import verbly.spring.domain.correction.exception.CorrectionHandler;
import verbly.spring.domain.correction.repository.*;
import verbly.spring.domain.post.enums.PostStatus;
import verbly.spring.domain.user.entity.User;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.security.utils.SecurityUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CorrectionNativeService {
    private static final int MAX_SIZE = 50;

    private final CorrectionRepository correctionRepository;
    private final CorrectionQueryRepository correctionQueryRepository;
    private final CorrectionWordRepository correctionWordRepository;
    private final CorrectionFeedbackRepository correctionFeedbackRepository;
    private final CorrectionEditorQueryRepository correctionEditorQueryRepository;
    private final CorrectionBookmarkRepository correctionBookmarkRepository;
    private final CorrectionWordEditApplier correctionWordEditApplier;

    public CorrectionResponseDTO.NativeCorrectionDTO getNativeCorrectionRequests(
            Boolean bookmark,
            PostStatus status,
            Pageable pageable
    ) {
        validateNativeAccess();

        Long userId = SecurityUtils.getCurrentUserId();

        Pageable safePageable = normalize(pageable);

        Page<CorrectionResponseDTO.MyCorrectionDto> pageResult =
                correctionQueryRepository.findNativeCorrectionRequests(
                        userId,
                        bookmark,
                        status,
                        safePageable
                );

        List<Long> correctionIds = pageResult.getContent().stream()
                .map(CorrectionResponseDTO.MyCorrectionDto::getCorrectionId)
                .distinct()
                .toList();

        Map<Long, Integer> wordCountMap = correctionWordRepository.countWordsByCorrectionIds(correctionIds).stream()
                .collect(Collectors.toMap(
                        CorrectionWordRepository.CorrectionCountRow::getCorrectionId,
                        r -> r.getCnt().intValue()
                ));

        Page<CorrectionResponseDTO.MyCorrectionDto> enriched =
                pageResult.map(dto -> CorrectionResponseDTO.MyCorrectionDto.builder()
                        .correctionId(dto.getCorrectionId())
                        .postId(dto.getPostId())
                        .title(dto.getTitle())
                        .status(dto.getStatus())
                        .bookmark(dto.getBookmark())
                        .content(dto.getContent())
                        .tags(dto.getTags())
                        .correctorType(dto.getCorrectorType())
                        .correctorName(dto.getCorrectorName())
                        .correctionCreatedAt(dto.getCorrectionCreatedAt())
                        .correctionUpdatedAt(dto.getCorrectionUpdatedAt())
                        .wordCount(wordCountMap.getOrDefault(dto.getCorrectionId(), 0))
                        .build()
                );

        long total =
                correctionQueryRepository.countNativeCorrectionRequests(
                        userId,
                        bookmark,
                        status
                );

        return CorrectionResponseDTO.NativeCorrectionDTO.from(enriched, total);
    }

    @Transactional(readOnly = true)
    public CorrectionEditorResponseDTO.Detail getDetail(Long correctionId) {
        validateNativeAccess();

        CorrectionEditorQueryDTO.CorrectionBaseRow base = findBaseOrThrow(correctionId);

        List<CorrectionWord> words = correctionWordRepository.findByCorrectionId(correctionId);

        String correctedContent = composeCorrectedContent(base.getPostContent(), words);

        List<CorrectionEditorResponseDTO.Sentence> sentences =
                CorrectionEditorConverter.toSentences(base.getPostContent(), correctedContent);

        List<CorrectionEditorResponseDTO.Word> wordResponses =
                CorrectionEditorConverter.toWordResponses(
                        correctionEditorQueryRepository.findWords(correctionId)
                );

        List<CorrectionEditorResponseDTO.Feedback> feedback =
                CorrectionEditorConverter.toFeedbackResponses(
                        correctionEditorQueryRepository.findFeedback(correctionId)
                );

        return CorrectionEditorResponseDTO.Detail.builder()
                .correctionId(base.getCorrectionId())
                .postId(base.getPostId())
                .status(base.getStatus())
                .sentences(sentences)
                .words(wordResponses)
                .feedback(feedback)
                .build();
    }

    public void upsertWords(Long correctionId, CorrectionEditorRequestDTO.UpsertWords request) {
        validateNativeAccess();

        Correction correction = findCorrectionOrThrow(correctionId);

        requireAiFinishedPendingForNativeStart(correction);

        boolean firstAction = isFirstNativeAction(correction);
        takeIfFirstNativeActionOrVerifyOwner(correction, firstAction);

        correctionWordEditApplier.applyWordIdEditsOrThrow(correction, request.getEdits());
    }

    public CorrectionEditorResponseDTO.WriteFeedbackResult writeFeedback(
            Long correctionId,
            CorrectorType correctorType,
            CorrectionEditorRequestDTO.WriteFeedback request
    ) {
        validateNativeAccess();

        Correction correction = findCorrectionOrThrow(correctionId);

        requireAiFinishedPendingForNativeStart(correction);

        if (request.getSentenceIdx() != null) {
            validateSentenceIdx(correction.getPost().getContent(), request.getSentenceIdx());
        }

        boolean firstAction = isFirstNativeAction(correction);
        takeIfFirstNativeActionOrVerifyOwner(correction, firstAction);

        User corrector = SecurityUtils.getCurrentUser();

        CorrectionFeedback saved = correctionFeedbackRepository.save(
                CorrectionEditorConverter.toFeedbackEntity(
                        correction,
                        request.getSentenceIdx(),
                        corrector,
                        correctorType,
                        request.getContent()
                )
        );

        return CorrectionEditorResponseDTO.WriteFeedbackResult.builder()
                .feedbackId(saved.getId())
                .build();
    }

    @Transactional(readOnly = true)
    public List<CorrectionEditorResponseDTO.Feedback> getFeedback(Long correctionId) {
        validateNativeAccess();

        findBaseOrThrow(correctionId);
        return CorrectionEditorConverter.toFeedbackResponses(
                correctionEditorQueryRepository.findFeedback(correctionId)
        );
    }

    public void submitCorrection(Long correctionId) {
        validateNativeAccess();

        Correction correction = findCorrectionOrThrow(correctionId);

        if (correction.getPost().getStatus() == PostStatus.COMPLETED) {
            throw new CorrectionHandler(ErrorStatus.CORRECTION_ALREADY_COMPLETED);
        }

        List<CorrectionWord> words = correctionWordRepository.findByCorrectionId(correctionId);
        if (words.isEmpty()) {
            throw new CorrectionHandler(ErrorStatus.CORRECTION_WORD_NOT_FOUND);
        }

        composeCorrectedContent(correction.getPost().getContent(), words);

        correction.getPost().changeStatus(PostStatus.COMPLETED);
    }

    public void updateFeedback(Long correctionId, Long feedbackId, CorrectionEditorRequestDTO.UpdateFeedback request) {
        validateNativeAccess();

        Correction correction = findCorrectionOrThrow(correctionId);
        takeIfFirstNativeActionOrVerifyOwner(correction, false);

        CorrectionFeedback feedback = findFeedbackOrThrow(feedbackId);
        validateFeedbackBelongsToCorrection(correctionId, feedback);
        validateFeedbackOwner(feedback);

        feedback.updateContent(request.getContent());
    }

    public void deleteFeedback(Long correctionId, Long feedbackId) {
        validateNativeAccess();

        Correction correction = findCorrectionOrThrow(correctionId);
        takeIfFirstNativeActionOrVerifyOwner(correction, false);

        CorrectionFeedback feedback = findFeedbackOrThrow(feedbackId);
        validateFeedbackBelongsToCorrection(correctionId, feedback);
        validateFeedbackOwner(feedback);

        correctionFeedbackRepository.delete(feedback);
    }

    public void addBookmark(Long correctionId) {
        validateNativeAccess();

        User user = SecurityUtils.getCurrentUser();
        Correction correction = findCorrectionOrThrow(correctionId);

        if (correctionBookmarkRepository.existsByUserIdAndCorrectionId(user.getId(), correctionId)) {
            return;
        }

        correctionBookmarkRepository.save(
                CorrectionBookmark.builder()
                        .user(user)
                        .correction(correction)
                        .build()
        );
    }

    public void removeBookmark(Long correctionId) {
        validateNativeAccess();

        Long userId = SecurityUtils.getCurrentUserId();
        correctionBookmarkRepository.deleteByUserIdAndCorrectionId(userId, correctionId);
    }

    private boolean isFirstNativeAction(Correction correction) {
        return correction.getPost().getStatus() == PostStatus.PENDING
                && correction.getCorrectorType() == CorrectorType.AI_ASSISTANT;
    }

    private void takeIfFirstNativeActionOrVerifyOwner(Correction correction, boolean isFirstNativeAction) {
        User current = SecurityUtils.getCurrentUser();

        if (isFirstNativeAction) {
            requirePendingForFirstAction(correction);

            if (correction.getCorrectorType() != CorrectorType.AI_ASSISTANT) {
                throw new CorrectionHandler(ErrorStatus.CORRECTION_AI_FIRST);
            }

            correction.takeoverByNative(current);
            markInProgressIfPending(correction);
        } else {
            requireInProgressAndOwner(correction);
        }
    }

    private Pageable normalize(Pageable pageable) {
        int page = Math.max(pageable.getPageNumber(), 0);
        int size = pageable.getPageSize() <= 0 ? 10 : Math.min(pageable.getPageSize(), MAX_SIZE);

        Sort sort = pageable.getSort().isSorted()
                ? pageable.getSort()
                : Sort.by(Sort.Direction.DESC, "id");

        return PageRequest.of(page, size, sort);
    }

    private void validateNativeAccess() {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null || !"en".equalsIgnoreCase(currentUser.getNativeLang())) {
            throw new CorrectionHandler(ErrorStatus.CORRECTION_NATIVE_ACCESS_DENIED);
        }
    }

    private CorrectionEditorQueryDTO.CorrectionBaseRow findBaseOrThrow(Long correctionId) {
        return correctionEditorQueryRepository.findCorrectionBase(correctionId)
                .orElseThrow(() -> new CorrectionHandler(ErrorStatus.CORRECTION_NOT_FOUND));
    }

    private Correction findCorrectionOrThrow(Long correctionId) {
        return correctionRepository.findById(correctionId)
                .orElseThrow(() -> new CorrectionHandler(ErrorStatus.CORRECTION_NOT_FOUND));
    }

    private String composeCorrectedContent(String originalContent, List<CorrectionWord> words) {
        List<String> sentences = new ArrayList<>(CorrectionEditorConverter.splitSentences(originalContent));

        Map<Integer, List<CorrectionWord>> grouped =
                words.stream().collect(Collectors.groupingBy(CorrectionWord::getSentenceIdx));

        for (var entry : grouped.entrySet()) {
            int sentenceIdx = entry.getKey();
            List<CorrectionWord> sentenceWords = entry.getValue();

            if (sentenceIdx < 0 || sentenceIdx >= sentences.size()) {
                throw new CorrectionHandler(ErrorStatus.CORRECTION_SENTENCE_INDEX_OUT_OF_RANGE);
            }

            sentenceWords.sort(Comparator.comparingInt(CorrectionWord::getStartIdx));

            String sentence = sentences.get(sentenceIdx);
            StringBuilder sb = new StringBuilder(sentence);

            int offset = 0;
            for (CorrectionWord w : sentenceWords) {
                int start = w.getStartIdx() + offset;
                int end = w.getEndIdx() + offset;

                if (start < 0 || end > sb.length() || start > end) {
                    throw new CorrectionHandler(ErrorStatus.CORRECTION_SENTENCE_INDEX_OUT_OF_RANGE);
                }

                sb.replace(start, end, w.getCorrectedText());
                offset += w.getCorrectedText().length() - (w.getEndIdx() - w.getStartIdx());
            }

            sentences.set(sentenceIdx, sb.toString());
        }

        return String.join(" ", sentences);
    }

    private void markInProgressIfPending(Correction correction) {
        if (correction.getPost().getStatus() == PostStatus.PENDING) {
            correction.getPost().changeStatus(PostStatus.IN_PROGRESS);
        }
    }

    private void validateSentenceIdx(String postContent, Integer sentenceIdx) {
        List<String> sentences = CorrectionEditorConverter.splitSentences(postContent);
        if (sentenceIdx == null || sentenceIdx < 0 || sentenceIdx >= sentences.size()) {
            throw new CorrectionHandler(ErrorStatus.CORRECTION_SENTENCE_INDEX_OUT_OF_RANGE);
        }
    }

    private void requirePendingForFirstAction(Correction correction) {
        if (correction.getPost().getStatus() != PostStatus.PENDING) {
            throw new CorrectionHandler(ErrorStatus.CORRECTION_FIRST_ACTION_ONLY_PENDING);
        }
    }

    private void requireInProgressAndOwner(Correction correction) {
        if (correction.getPost().getStatus() != PostStatus.IN_PROGRESS) {
            throw new CorrectionHandler(ErrorStatus.CORRECTION_EDIT_ONLY_IN_PROGRESS);
        }

        User current = SecurityUtils.getCurrentUser();
        if (current == null || correction.getCorrector() == null
                || !correction.getCorrector().getId().equals(current.getId())) {
            throw new CorrectionHandler(ErrorStatus.CORRECTION_NOT_THE_CORRECTOR);
        }
    }

    private CorrectionFeedback findFeedbackOrThrow(Long feedbackId) {
        return correctionFeedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new CorrectionHandler(ErrorStatus.CORRECTION_FEEDBACK_NOT_FOUND));
    }

    private void validateFeedbackBelongsToCorrection(Long correctionId, CorrectionFeedback feedback) {
        if (!feedback.getCorrection().getId().equals(correctionId)) {
            throw new CorrectionHandler(ErrorStatus.CORRECTION_ACCESS_DENIED);
        }
    }

    private void validateFeedbackOwner(CorrectionFeedback feedback) {
        User current = SecurityUtils.getCurrentUser();
        if (current == null || feedback.getCorrector() == null
                || !feedback.getCorrector().getId().equals(current.getId())) {
            throw new CorrectionHandler(ErrorStatus.CORRECTION_FEEDBACK_ACCESS_DENIED);
        }
    }

    private void requireAiFinishedPendingForNativeStart(Correction correction) {
        if (correction.getPost().getStatus() != PostStatus.PENDING) {
            throw new CorrectionHandler(ErrorStatus.CORRECTION_FIRST_ACTION_ONLY_PENDING);
        }
        if (correction.getCorrectorType() != CorrectorType.AI_ASSISTANT) {

            throw new CorrectionHandler(ErrorStatus.CORRECTION_AI_FIRST);
        }
    }
}
