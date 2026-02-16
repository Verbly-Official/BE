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

    //  en=원형(lemma), ko=뜻, exEn/exKo=원형 포함 예문/뜻
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

        if (correction.getPost() == null
                || correction.getPost().getAuthor() == null
                || !Objects.equals(correction.getPost().getAuthor().getId(), userId)) {
            throw new CorrectionHandler(ErrorStatus.CORRECTION_ACCESS_DENIED);
        }

        Long postId = correction.getPost().getId();

        List<ExampleInput> safeInputs = (inputs == null) ? List.of() : inputs.stream()
                .filter(i -> i != null && i.en() != null && !i.en().isBlank())
                .map(i -> new ExampleInput(
                        cleanPhraseForItem(i.en()),
                        (i.ko() == null ? null : i.ko().trim()),
                        normalizeSpacesKeepCase(i.exEn()),
                        (i.exKo() == null ? null : i.exKo().trim())
                ))
                .toList();

        int itemsTouched = 0;
        int sourcesInserted = 0;
        int examplesInserted = 0;

        for (ExampleInput in : safeInputs) {
            String lemma = safe(in.en());
            String meaningKo = safe(in.ko());
            String lemmaNorm = LibraryConverter.normalizePhrase(lemma);
            if (lemmaNorm.isBlank()) continue;

            // 1) item upsert (en/ko 저장)
            LibraryItem item = upsertItem(userId, lemma, lemmaNorm, meaningKo, null);
            itemsTouched++;

            // 2)  source는 correctionId -> postId만 넣고 저장 (correction_word 연결 X)
            if (!libraryItemSourceRepository.existsByLibraryItem_IdAndCorrectionId(item.getId(), correctionId)) {
                LibraryItemSource src = LibraryItemSource.ofCorrection(item, postId, correctionId);
                libraryItemSourceRepository.save(src);
                sourcesInserted++;
            }

            // 3) example 저장 (lemma 포함 검증)
            String exampleEn = safe(in.exEn());
            String exampleKo = safe(in.exKo());

            if (exampleEn.isBlank() || !containsFlexible(exampleEn, lemmaNorm)) {
                exampleEn = "I want to " + lemma + ".";
            }
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
