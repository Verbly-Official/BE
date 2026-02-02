package verbly.spring.domain.correction.converter;

import verbly.spring.domain.correction.dto.request.CorrectionEditorRequestDTO;
import verbly.spring.domain.correction.dto.response.CorrectionEditorQueryDTO;
import verbly.spring.domain.correction.dto.response.CorrectionEditorResponseDTO;
import verbly.spring.domain.correction.entity.Correction;
import verbly.spring.domain.correction.entity.CorrectionFeedback;
import verbly.spring.domain.correction.entity.CorrectionWord;
import verbly.spring.domain.correction.enums.CorrectorType;
import verbly.spring.domain.user.entity.User;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class CorrectionEditorConverter {
    private CorrectionEditorConverter() {}

    public static List<CorrectionEditorResponseDTO.Sentence> toSentences(String postContent) {
        if (postContent == null || postContent.isBlank()) return List.of();

        String[] parts = postContent.split("\\n");
        if (parts.length == 1 && parts[0].isBlank()) return List.of();

        return IntStream.range(0, parts.length)
                .mapToObj(i -> CorrectionEditorResponseDTO.Sentence.builder()
                        .idx(i + 1)
                        .originalText(parts[i].trim())
                        .build())
                .toList();
    }

    public static List<CorrectionEditorResponseDTO.Sentence> toSentences(
            String originalContent,
            String correctedContent
    ) {
        if (originalContent == null || originalContent.isBlank()) return List.of();

        String[] originalParts = originalContent.split("\\n");
        String[] correctedParts = correctedContent == null
                ? new String[0]
                : correctedContent.split("\\n");

        int size = Math.min(originalParts.length, correctedParts.length);

        return IntStream.range(0, size)
                .mapToObj(i -> CorrectionEditorResponseDTO.Sentence.builder()
                        .idx(i + 1)
                        .originalText(originalParts[i].trim())
                        .correctedText(correctedParts[i].trim())
                        .build())
                .toList();
    }


    public static List<CorrectionWord> toWordEntities(Correction correction, CorrectionEditorRequestDTO.UpsertWords request) {
        if (request == null || request.getEdits() == null || request.getEdits().isEmpty()) {
            return List.of();
        }

        return request.getEdits().stream()
                .map(e -> CorrectionWord.builder()
                        .correction(correction)
                        .sentenceIdx(e.getSentenceIdx())
                        .startIdx(e.getStartIdx())
                        .endIdx(e.getEndIdx())
                        .originalText(e.getOriginalText())
                        .correctedText(e.getCorrectedText())
                        .build())
                .toList();
    }

    public static List<CorrectionEditorResponseDTO.Word> toWordResponses(List<CorrectionEditorQueryDTO.WordRow> rows) {
        return rows.stream()
                .map(r -> CorrectionEditorResponseDTO.Word.builder()
                        .wordId(r.getWordId())
                        .sentenceIdx(r.getSentenceIdx())
                        .startIdx(r.getStartIdx())
                        .endIdx(r.getEndIdx())
                        .originalText(r.getOriginalText())
                        .correctedText(r.getCorrectedText())
                        .build())
                .toList();
    }

    public static List<CorrectionEditorResponseDTO.Feedback> toFeedbackResponses(List<CorrectionEditorQueryDTO.FeedbackRow> rows) {
        return rows.stream()
                .map(r -> CorrectionEditorResponseDTO.Feedback.builder()
                        .feedbackId(r.getFeedbackId())
                        .wordId(r.getWordId())
                        .correctorName(r.getCorrectorName())
                        .correctorType(r.getCorrectorType())
                        .content(r.getContent())
                        .createdAt(r.getCreatedAt())
                        .updatedAt(r.getUpdatedAt())
                        .build())
                .toList();
    }

    public static CorrectionFeedback toFeedbackEntity(
            Correction correction,
            CorrectionWord word,
            User corrector,
            CorrectorType correctorType,
            String content
    ) {
        return CorrectionFeedback.builder()
                .correction(correction)
                .correctionWord(word)
                .corrector(corrector)
                .correctorType(correctorType)
                .content(content)
                .build();
    }

    public static List<String> splitSentences(String content) {
        if (content == null || content.isBlank()) {
            return List.of();
        }

        return Arrays.stream(
                        content.split("(?<=[.!?])\\s+|\n")
                )
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
