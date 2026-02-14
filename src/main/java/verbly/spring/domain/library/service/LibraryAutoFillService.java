package verbly.spring.domain.library.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.correction.converter.CorrectionEditorConverter;
import verbly.spring.domain.correction.entity.Correction;
import verbly.spring.domain.correction.entity.CorrectionWord;
import verbly.spring.domain.correction.exception.CorrectionHandler;
import verbly.spring.domain.correction.repository.CorrectionRepository;
import verbly.spring.domain.correction.repository.CorrectionWordRepository;
import verbly.spring.domain.library.converter.LibraryConverter;
import verbly.spring.domain.library.entity.LibraryItem;
import verbly.spring.domain.library.entity.LibraryItemExample;
import verbly.spring.domain.library.entity.LibraryItemSource;
import verbly.spring.domain.library.enums.ExampleSource;
import verbly.spring.domain.library.enums.LibraryItemStatus;
import verbly.spring.domain.library.repository.LibraryItemExampleRepository;
import verbly.spring.domain.library.repository.LibraryItemRepository;
import verbly.spring.domain.library.repository.LibraryItemSourceRepository;
import verbly.spring.global.common.code.ErrorStatus;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LibraryAutoFillService {

    public record ExampleInput(String exEn, String exKo) {}
    public record FillResult(int itemsTouched, int sourcesInserted, int examplesInserted) {}

    private final CorrectionRepository correctionRepository;
    private final CorrectionWordRepository correctionWordRepository;

    private final LibraryItemRepository libraryItemRepository;
    private final LibraryItemSourceRepository libraryItemSourceRepository;
    private final LibraryItemExampleRepository libraryItemExampleRepository;

    /**
     * correction_word가 업데이트된 "직후" 호출:
     * library_items / library_item_sources / library_item_examples 채움
     */
    public FillResult fillFromCorrection(Long userId, Long correctionId, List<ExampleInput> examples) {
        Correction correction = correctionRepository.findById(correctionId)
                .orElseThrow(() -> new CorrectionHandler(ErrorStatus.CORRECTION_NOT_FOUND));

        // 작성자 검증
        if (correction.getPost() == null
                || correction.getPost().getAuthor() == null
                || !Objects.equals(correction.getPost().getAuthor().getId(), userId)) {
            throw new CorrectionHandler(ErrorStatus.CORRECTION_ACCESS_DENIED);
        }

        List<CorrectionWord> words = correctionWordRepository.findByCorrectionId(correctionId);
        if (words.isEmpty()) {
            throw new CorrectionHandler(ErrorStatus.CORRECTION_WORD_NOT_FOUND);
        }

        Long postId = correction.getPost().getId();

        // fallback 예문용: "최종 교정 문장" 만들어두기
        List<String> correctedSentences = buildCorrectedSentences(correction.getPost().getContent(), words);

        // examples 정리
        List<ExampleInput> safeExamples = (examples == null) ? List.of() : examples.stream()
                .filter(e -> e != null && e.exEn() != null && !e.exEn().isBlank())
                .map(e -> new ExampleInput(normalizeSpacesKeepCase(e.exEn()), e.exKo() == null ? null : e.exKo().trim()))
                .toList();

        int itemsTouched = 0;
        int sourcesInserted = 0;
        int examplesInserted = 0;

        for (CorrectionWord w : words) {
            String original = safe(w.getOriginalText());
            String correctedRaw = safe(w.getCorrectedText());

            if (correctedRaw.isBlank()) continue;

            // 라이브러리 phrase로 저장할 값(끝 punctuation 제거 정도만)
            String correctedForItem = cleanPhraseForItem(correctedRaw);
            String originalNorm = LibraryConverter.normalizePhrase(original);
            String correctedNorm = LibraryConverter.normalizePhrase(correctedForItem);

            // 실질 변경 없으면 스킵(띄어쓰기/대소문자만 변경 포함)
            if (originalNorm.equals(correctedNorm)) continue;
            if (correctedNorm.isBlank()) continue;

            // 1) library_items upsert
            LibraryItem item = upsertItem(userId, correctedForItem, correctedNorm);
            itemsTouched++;

            // 2) library_item_sources insert (중복 방지)
            if (!libraryItemSourceRepository.existsByLibraryItem_IdAndCorrectionWordId(item.getId(), w.getId())) {
                LibraryItemSource src = LibraryItemSource.ofCorrectionWord(
                        item,
                        postId,
                        correctionId,
                        w.getId(),
                        w.getSentenceIdx(),
                        w.getStartIdx(),
                        w.getEndIdx(),
                        original,
                        correctedRaw // source에는 원본 correctedRaw 그대로 보관(콤마 등 포함 가능)
                );
                libraryItemSourceRepository.save(src);
                sourcesInserted++;
            }

            // 3) library_item_examples insert
            //    ex 리스트에서 phrase 포함하는 예문들 저장
            List<ExampleInput> matched = matchExamplesByPhrase(safeExamples, correctedNorm);

            if (!matched.isEmpty()) {
                for (ExampleInput ex : matched) {
                    String exEnStore = normalizeSpacesKeepCase(ex.exEn());
                    if (libraryItemExampleRepository.existsByLibraryItem_IdAndExampleEn(item.getId(), exEnStore)) continue;

                    libraryItemExampleRepository.save(
                            LibraryItemExample.of(item, exEnStore, ex.exKo(), ExampleSource.AI)
                    );
                    examplesInserted++;
                }
            } else {
                // 없으면 fallback: 해당 sentenceIdx의 "최종 교정 문장"을 예문으로 1개 저장
                String fallback = pickFallbackSentence(correctedSentences, w.getSentenceIdx(), correctedForItem);

                // fallback도 정답 phrase 포함이 안 되면 더미로
                if (!containsFlexible(fallback, correctedNorm)) {
                    fallback = "I learned the expression \"" + correctedForItem + "\" today.";
                }

                String exEnStore = normalizeSpacesKeepCase(fallback);
                if (!libraryItemExampleRepository.existsByLibraryItem_IdAndExampleEn(item.getId(), exEnStore)) {
                    libraryItemExampleRepository.save(
                            LibraryItemExample.of(item, exEnStore, null, ExampleSource.AI)
                    );
                    examplesInserted++;
                }
            }
        }

        return new FillResult(itemsTouched, sourcesInserted, examplesInserted);
    }

    // ---------------- helpers ----------------

    private LibraryItem upsertItem(Long userId, String phrase, String phraseNorm) {
        return libraryItemRepository.findByUserIdAndPhraseNorm(userId, phraseNorm)
                .map(existing -> {
                    // soft delete였으면 살려두기(권장)
                    if (existing.getStatus() == LibraryItemStatus.DELETED) {
                        existing.reactivate();
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    try {
                        return libraryItemRepository.save(LibraryItem.of(userId, phrase, phraseNorm, null, null));
                    } catch (DataIntegrityViolationException e) {
                        // 동시성 유니크 충돌 시 재조회
                        return libraryItemRepository.findByUserIdAndPhraseNorm(userId, phraseNorm)
                                .orElseThrow(() -> e);
                    }
                });
    }

    private List<String> buildCorrectedSentences(String postContent, List<CorrectionWord> words) {
        List<String> sentences = new ArrayList<>(CorrectionEditorConverter.splitSentences(postContent));

        Map<Integer, List<CorrectionWord>> bySentence =
                words.stream().collect(Collectors.groupingBy(CorrectionWord::getSentenceIdx));

        for (var entry : bySentence.entrySet()) {
            int idx = entry.getKey();
            if (idx < 0 || idx >= sentences.size()) continue;

            String base = sentences.get(idx);
            StringBuilder sb = new StringBuilder(base);

            // 오른쪽→왼쪽으로 replace(인덱스 안 꼬임)
            List<CorrectionWord> list = new ArrayList<>(entry.getValue());
            list.sort(Comparator.comparingInt(CorrectionWord::getStartIdx).reversed());

            for (CorrectionWord w : list) {
                int start = w.getStartIdx();
                int end = w.getEndIdx();
                if (start < 0 || end > sb.length() || start > end) continue;

                sb.replace(start, end, w.getCorrectedText() == null ? "" : w.getCorrectedText());
            }

            sentences.set(idx, sb.toString());
        }

        return sentences;
    }

    private String pickFallbackSentence(List<String> correctedSentences, Integer sentenceIdx, String phrase) {
        if (correctedSentences == null || correctedSentences.isEmpty()) {
            return "I learned the expression \"" + phrase + "\" today.";
        }
        if (sentenceIdx != null && sentenceIdx >= 0 && sentenceIdx < correctedSentences.size()) {
            return correctedSentences.get(sentenceIdx);
        }
        return correctedSentences.get(0);
    }

    private List<ExampleInput> matchExamplesByPhrase(List<ExampleInput> examples, String phraseNorm) {
        if (examples == null || examples.isEmpty()) return List.of();
        if (phraseNorm == null || phraseNorm.isBlank()) return List.of();

        Pattern p = buildFlexibleBoundaryPattern(phraseNorm);

        return examples.stream()
                .filter(e -> e.exEn() != null && p.matcher(normalizeLowerSpace(e.exEn())).find())
                .toList();
    }

    // "take off" → "take\\s+off" + word boundary(영숫자 기준)
    private Pattern buildFlexibleBoundaryPattern(String phraseNorm) {
        String core = Arrays.stream(phraseNorm.split("\\s+"))
                .map(Pattern::quote)
                .collect(Collectors.joining("\\s+"));
        String regex = "(?i)(?<![A-Za-z0-9])" + core + "(?![A-Za-z0-9])";
        return Pattern.compile(regex);
    }

    private boolean containsFlexible(String text, String phraseNorm) {
        return buildFlexibleBoundaryPattern(phraseNorm).matcher(normalizeLowerSpace(text)).find();
    }

    private String normalizeLowerSpace(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private String normalizeSpacesKeepCase(String s) {
        return s == null ? "" : s.trim().replaceAll("\\s+", " ");
    }

    private String cleanPhraseForItem(String s) {
        if (s == null) return "";
        String t = s.trim();
        // 양 끝의 문장부호만 제거(내부 공백은 유지)
        t = t.replaceAll("^[\\p{Punct}]+", "");
        t = t.replaceAll("[\\p{Punct}]+$", "");
        return t.trim();
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
