package verbly.spring.domain.correction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import verbly.spring.domain.correction.dto.request.CorrectionEditorRequestDTO;
import verbly.spring.domain.correction.entity.Correction;
import verbly.spring.domain.correction.entity.CorrectionWord;
import verbly.spring.domain.correction.exception.CorrectionHandler;
import verbly.spring.domain.correction.repository.CorrectionWordRepository;
import verbly.spring.global.common.code.ErrorStatus;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CorrectionWordEditApplier {
    private final CorrectionWordRepository correctionWordRepository;

    public void applyWordIdEditsOrThrow(Correction correction, List<CorrectionEditorRequestDTO.WordEdit> edits) {
        if (edits == null || edits.isEmpty()) return;

        for (CorrectionEditorRequestDTO.WordEdit edit : edits) {
            CorrectionWord word = correctionWordRepository.findById(edit.getWordId())
                    .orElseThrow(() -> new CorrectionHandler(ErrorStatus.CORRECTION_WORD_NOT_FOUND));

            if (!word.getCorrection().getId().equals(correction.getId())) {
                throw new CorrectionHandler(ErrorStatus.CORRECTION_ACCESS_DENIED);
            }

            String newCorrected = (edit.getCorrectedText() == null) ? "" : edit.getCorrectedText();
            word.update(newCorrected, word.getStartIdx(), word.getEndIdx());
        }
    }
}
