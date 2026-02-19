package verbly.spring.domain.library.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.correction.entity.Correction;
import verbly.spring.domain.correction.exception.CorrectionHandler;
import verbly.spring.domain.correction.repository.CorrectionRepository;
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

    // ✅ en=원형(lemma), ko=뜻, examples=예문 리스트
    public record ExamplePairInput(String exEn, String exKo) {}
    public record ExampleInput(String en, String ko, List<ExamplePairInput> examples) {}
    public record FillResult(int itemsTouched, int sourcesInserted, int examplesInserted) {}

    private final CorrectionRepository correctionRepository;

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

        Long postId = correction.getPost().getId();

        // ✅ 입력 정리
        List<ExampleInput> safeInputs = (inputs == null) ? List.of() : inputs.stream()
                .filter(i -> i != null && i.en() != null && !i.en().isBlank())
                .map(i -> new ExampleInput(
                        cleanPhraseForItem(i.en()),
                        (i.ko() == null ? null : i.ko().trim()),
                        sanitizeExamples(i.examples())
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

            // 1) item upsert (✅ en/ko 저장)
            LibraryItem item = upsertItem(userId, lemma, lemmaNorm, meaningKo, null);
            itemsTouched++;

            // 2) source 저장: correctionId, postId만 (correction_word 연결 X)
            if (!libraryItemSourceRepository.existsByLibraryItem_IdAndCorrectionId(item.getId(), correctionId)) {
                LibraryItemSource src = LibraryItemSource.ofCorrection(item, postId, correctionId);
                libraryItemSourceRepository.save(src);
                sourcesInserted++;
            }

            // 3) example 저장: examples 리스트를 순회해서 lemma 포함 예문 저장
            int insertedForThisItem = 0;

            for (ExamplePairInput ex : in.examples()) {
                String exEn = normalizeSpacesKeepCase(ex.exEn());
                String exKo = ex.exKo() == null ? null : ex.exKo().trim();

                if (exEn.isBlank()) continue;
                if (!containsFlexible(exEn, lemmaNorm)) continue;

                if (libraryItemExampleRepository.existsByLibraryItem_IdAndExampleEn(item.getId(), exEn)) continue;

                libraryItemExampleRepository.save(
                        LibraryItemExample.of(item, exEn, (exKo == null || exKo.isBlank()) ? null : exKo, ExampleSource.AI)
                );
                examplesInserted++;
                insertedForThisItem++;
            }

            // ✅ lemma 포함 예문이 0개면 fallback 1개 저장
            if (insertedForThisItem == 0) {
                String fallback = "I want to " + lemma + ".";
                String exEnStore = normalizeSpacesKeepCase(fallback);

                if (!libraryItemExampleRepository.existsByLibraryItem_IdAndExampleEn(item.getId(), exEnStore)) {
                    libraryItemExampleRepository.save(
                            LibraryItemExample.of(item, exEnStore, (meaningKo == null || meaningKo.isBlank()) ? null : meaningKo, ExampleSource.AI)
                    );
                    examplesInserted++;
                }
            }
        }

        return new FillResult(itemsTouched, sourcesInserted, examplesInserted);
    }

    // ---------------- helpers ----------------

    private List<ExamplePairInput> sanitizeExamples(List<ExamplePairInput> examples) {
        if (examples == null) return List.of();
        return examples.stream()
                .filter(e -> e != null && e.exEn() != null && !e.exEn().isBlank())
                .map(e -> new ExamplePairInput(
                        normalizeSpacesKeepCase(e.exEn()),
                        (e.exKo() == null ? null : e.exKo().trim())
                ))
                .toList();
    }

    private LibraryItem upsertItem(Long userId, String phrase, String phraseNorm, String meaningKo, String meaningEn) {
        return libraryItemRepository.findByUserIdAndPhraseNorm(userId, phraseNorm)
                .map(existing -> {
                    if (existing.getStatus() == LibraryItemStatus.DELETED) {
                        existing.reactivate(); // ✅ 아래에서 엔티티에 추가할 메서드
                    }
                    // meaning이 비어있으면 채우기(덮어쓰기 X)
                    if ((existing.getMeaningKo() == null || existing.getMeaningKo().isBlank())
                            && meaningKo != null && !meaningKo.isBlank()) {
                        existing.updateMeanings(meaningKo, null); // ✅ 엔티티에 추가
                    }
                    if ((existing.getMeaningEn() == null || existing.getMeaningEn().isBlank())
                            && meaningEn != null && !meaningEn.isBlank()) {
                        existing.updateMeanings(null, meaningEn); // ✅ 엔티티에 추가
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

    // "take off" → "take\\s+off" + 경계
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
