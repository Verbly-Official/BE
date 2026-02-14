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

    // ✅ en=원형(lemma), ko=뜻, exEn/exKo=원형 포함 예문/뜻
    public record ExampleInput(String en, String ko, String exEn, String exKo) {}
    public record FillResult(int itemsTouched, int sourcesInserted, int examplesInserted) {}

    private final CorrectionRepository correctionRepository;
    private final CorrectionWordRepository correctionWordRepository;

    private final LibraryItemRepository libraryItemRepository;
    private final LibraryItemSourceRepository libraryItemSourceRepository;
    private final LibraryItemExampleRepository libraryItemExampleRepository;

    public FillResult fillFromCorrection(Long userId, Long correctionId, List<ExampleInput> inputs) {
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

        // fallback 예문용(원형 예문이 없을 때)
        List<String> correctedSentences = buildCorrectedSentences(correction.getPost().getContent(), words);

        // 입력 정리
        List<ExampleInput> safeInputs = (inputs == null) ? List.of() : inputs.stream()
                .filter(i -> i != null && i.en() != null && !i.en().isBlank())
                .map(i -> new ExampleInput(
                        cleanPhraseForItem(i.en()),
                        (i.ko() == null ? null : i.ko().trim()),
                        normalizeSpacesKeepCase(i.exEn()),
                        (i.exKo() == null ? null : i.exKo().trim())
                ))
                .toList();

        // correctionWord 인덱싱(원문/교정문 기준)
        Map<String, List<CorrectionWord>> byOriginal = indexByNorm(words, true);
        Map<String, List<CorrectionWord>> byCorrected = indexByNorm(words, false);

        int itemsTouched = 0;
        int sourcesInserted = 0;
        int examplesInserted = 0;

        // ✅ 이제는 inputs(=라이브러리 카드) 기준으로 저장
        for (ExampleInput in : safeInputs) {
            String lemma = safe(in.en());
            String meaningKo = safe(in.ko());
            String lemmaNorm = LibraryConverter.normalizePhrase(lemma);

            if (lemmaNorm.isBlank()) continue;

            // 1) item upsert (✅ meaningKo까지 채움)
            LibraryItem item = upsertItem(userId, lemma, lemmaNorm, meaningKo, null);
            itemsTouched++;

            // 2) source 연결: lemma가 originalText 또는 correctedText에 있으면 연결 가능
            List<CorrectionWord> candidates = new ArrayList<>();
            if (byOriginal.containsKey(lemmaNorm)) candidates.addAll(byOriginal.get(lemmaNorm));
            if (byCorrected.containsKey(lemmaNorm)) candidates.addAll(byCorrected.get(lemmaNorm));

            // 중복 제거(같은 word가 양쪽 매칭될 수 있음)
            candidates = candidates.stream().distinct().toList();

            for (CorrectionWord w : candidates) {
                if (libraryItemSourceRepository.existsByLibraryItem_IdAndCorrectionWordId(item.getId(), w.getId())) {
                    continue;
                }

                LibraryItemSource src = LibraryItemSource.ofCorrectionWord(
                        item,
                        postId,
                        correctionId,
                        w.getId(),
                        w.getSentenceIdx(),
                        w.getStartIdx(),
                        w.getEndIdx(),
                        safe(w.getOriginalText()),
                        safe(w.getCorrectedText())
                );
                libraryItemSourceRepository.save(src);
                sourcesInserted++;
            }

            // 3) example 저장 (원형 포함 예문이 없으면 fallback)
            String exampleEn = safe(in.exEn());
            String exampleKo = safe(in.exKo());

            if (exampleEn.isBlank() || !containsFlexible(exampleEn, lemmaNorm)) {
                exampleEn = "I want to " + lemma + ".";
            }
            // 힌트 비지 않게: 예문 뜻이 없으면 meaningKo로 채우는 건 선택(원하면 제거 가능)
            if (exampleKo.isBlank()) exampleKo = meaningKo;

            String exEnStore = normalizeSpacesKeepCase(exampleEn);
            if (!libraryItemExampleRepository.existsByLibraryItem_IdAndExampleEn(item.getId(), exEnStore)) {
                libraryItemExampleRepository.save(
                        LibraryItemExample.of(item, exEnStore, exampleKo.isBlank() ? null : exampleKo, ExampleSource.AI)
                );
                examplesInserted++;
            }
        }

        return new FillResult(itemsTouched, sourcesInserted, examplesInserted);
    }

    // ---------------- helpers ----------------

    private LibraryItem upsertItem(Long userId, String phrase, String phraseNorm, String meaningKo, String meaningEn) {
        return libraryItemRepository.findByUserIdAndPhraseNorm(userId, phraseNorm)
                .map(existing -> {
                    if (existing.getStatus() == LibraryItemStatus.DELETED) {
                        existing.reactivate();
                    }
                    // 기존 meaning이 비어있으면 채움(덮어쓰기 정책은 팀 합의대로)
                    if ((existing.getMeaningKo() == null || existing.getMeaningKo().isBlank())
                            && meaningKo != null && !meaningKo.isBlank()) {
                        existing.updateMeanings(meaningKo, null);
                    }
                    if ((existing.getMeaningEn() == null || existing.getMeaningEn().isBlank())
                            && meaningEn != null && !meaningEn.isBlank()) {
                        existing.updateMeanings(null, meaningEn);
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    try {
                        return libraryItemRepository.save(LibraryItem.of(userId, phrase, phraseNorm, meaningKo, meaningEn));
                    } catch (DataIntegrityViolationException e) {
                        return libraryItemRepository.findByUserIdAndPhraseNorm(userId, phraseNorm)
                                .orElseThrow(() -> e);
                    }
                });
    }

    private Map<String, List<CorrectionWord>> indexByNorm(List<CorrectionWord> words, boolean useOriginal) {
        Map<String, List<CorrectionWord>> map = new HashMap<>();
        for (CorrectionWord w : words) {
            String t = useOriginal ? safe(w.getOriginalText()) : safe(w.getCorrectedText());
            String norm = LibraryConverter.normalizePhrase(cleanPhraseForItem(t));
            if (norm.isBlank()) continue;
            map.computeIfAbsent(norm, k -> new ArrayList<>()).add(w);
        }
        return map;
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
        t = t.replaceAll("^[\\p{Punct}]+", "");
        t = t.replaceAll("[\\p{Punct}]+$", "");
        return t.trim();
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
