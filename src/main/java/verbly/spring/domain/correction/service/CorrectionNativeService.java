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
import verbly.spring.domain.correction.entity.CorrectionFeedback;
import verbly.spring.domain.correction.entity.CorrectionWord;
import verbly.spring.domain.correction.enums.CorrectorType;
import verbly.spring.domain.correction.exception.CorrectionHandler;
import verbly.spring.domain.correction.repository.*;
import verbly.spring.domain.post.enums.PostStatus;
import verbly.spring.domain.user.entity.User;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.security.utils.SecurityUtils;

import java.util.List;

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

    public Page<CorrectionResponseDTO.MyCorrectionDto> getNativeCorrectionRequests(PostStatus status, Pageable pageable) {
        validateNativeAccess();

        Pageable safePageable = normalize(pageable);
        return correctionQueryRepository.findNativeCorrectionRequests(status, safePageable);
    }

    @Transactional(readOnly = true)
    public CorrectionEditorResponseDTO.Detail getDetail(Long correctionId) {
        CorrectionEditorQueryDTO.CorrectionBaseRow base = findBaseOrThrow(correctionId);

        List<CorrectionEditorResponseDTO.Sentence> sentences =
                CorrectionEditorConverter.toSentences(base.getPostContent());

        List<CorrectionEditorResponseDTO.Word> words =
                CorrectionEditorConverter.toWordResponses(correctionEditorQueryRepository.findWords(correctionId));

        List<CorrectionEditorResponseDTO.Feedback> feedback =
                CorrectionEditorConverter.toFeedbackResponses(correctionEditorQueryRepository.findFeedback(correctionId));

        return CorrectionEditorResponseDTO.Detail.builder()
                .correctionId(base.getCorrectionId())
                .postId(base.getPostId())
                .sentences(sentences)
                .words(words)
                .feedback(feedback)
                .build();
    }

    public void upsertWords(Long correctionId, CorrectionEditorRequestDTO.UpsertWords request) {
        Correction correction = findCorrectionOrThrow(correctionId);

        correctionWordRepository.deleteByCorrectionId(correctionId);

        List<CorrectionWord> entities = CorrectionEditorConverter.toWordEntities(correction, request);
        if (!entities.isEmpty()) {
            correctionWordRepository.saveAll(entities);
        }
    }

    public CorrectionEditorResponseDTO.WriteFeedbackResult writeFeedback(
            Long correctionId,
            CorrectorType correctorType,
            CorrectionEditorRequestDTO.WriteFeedback request
    ) {
        Correction correction = findCorrectionOrThrow(correctionId);
        CorrectionWord word = findWordOrThrow(request.getCorrectionWordId());

        validateWordBelongsToCorrection(correction, word);

        User corrector = SecurityUtils.getCurrentUser();

        CorrectionFeedback saved = correctionFeedbackRepository.save(
                CorrectionEditorConverter.toFeedbackEntity(
                        correction,
                        word,
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
        findBaseOrThrow(correctionId);
        return CorrectionEditorConverter.toFeedbackResponses(
                correctionEditorQueryRepository.findFeedback(correctionId)
        );
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

    private CorrectionWord findWordOrThrow(Long wordId) {
        return correctionWordRepository.findById(wordId)
                .orElseThrow(() -> new CorrectionHandler(ErrorStatus.CORRECTION_WORD_NOT_FOUND));
    }

    private void validateWordBelongsToCorrection(Correction correction, CorrectionWord word) {
        if (!word.getCorrection().getId().equals(correction.getId())) {
            throw new CorrectionHandler(ErrorStatus.CORRECTION_ACCESS_DENIED);
        }
    }
}
