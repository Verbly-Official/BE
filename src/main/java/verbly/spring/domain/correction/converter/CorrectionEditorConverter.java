package verbly.spring.domain.correction.converter;

import verbly.spring.domain.correction.dto.response.CorrectionEditorQueryDTO;
import verbly.spring.domain.correction.dto.response.CorrectionEditorResponseDTO;
import verbly.spring.domain.correction.entity.Correction;
import verbly.spring.domain.correction.entity.CorrectionFeedback;
import verbly.spring.domain.correction.enums.CorrectorType;
import verbly.spring.domain.user.entity.User;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class CorrectionEditorConverter {
    private CorrectionEditorConverter() {}

    public static List<CorrectionEditorResponseDTO.Sentence> toSentences(String postContent) {
        if (postContent == null || postContent.isBlank()) return List.of();

        String normalized = normalizeLineBreaks(postContent);
        if (normalized.isBlank()) return List.of();

        List<String> lines = Arrays.stream(normalized.split("\\n+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        return IntStream.range(0, lines.size())
                .mapToObj(i -> CorrectionEditorResponseDTO.Sentence.builder()
                        .sentenceIdx(i)
                        .originalText(lines.get(i))
                        .build())
                .toList();
    }

    public static List<CorrectionEditorResponseDTO.Sentence> toSentences(
            String originalContent,
            String correctedContent
    ) {
        List<String> originals = splitSentences(normalizeLineBreaks(originalContent));
        List<String> corrected = splitSentences(normalizeLineBreaks(correctedContent));

        int maxSize = Math.max(originals.size(), corrected.size());

        return IntStream.range(0, maxSize)
                .mapToObj(i -> CorrectionEditorResponseDTO.Sentence.builder()
                        .sentenceIdx(i)
                        .originalText(i < originals.size() ? originals.get(i) : null)
                        .correctedText(i < corrected.size() ? corrected.get(i) : null)
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
                        .sentenceIdx(r.getSentenceIdx())
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
            Integer sentenceIdx,
            User corrector,
            CorrectorType correctorType,
            String content
    ) {
        return CorrectionFeedback.builder()
                .correction(correction)
                .sentenceIdx(sentenceIdx)
                .corrector(corrector)
                .correctorType(correctorType)
                .content(content)
                .build();
    }

    public static List<String> splitSentences(String content) {
        if (content == null || content.isBlank()) {
            return List.of();
        }

        String normalized = normalizeLineBreaks(content);
        if (normalized.isBlank()) return List.of();

        return Arrays.stream(normalized.split("(?<=[.!?])\\s+|\\n+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private static String normalizeLineBreaks(String s) {
        if (s == null) return "";

        s = s.replace("\\r\\n", "\n")
                .replace("\\n", "\n")
                .replace("\\r", "\n");

        s = s.replace("\r\n", "\n")
                .replace("\r", "\n");

        return s;
    }
}
