package verbly.spring.domain.correction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.correction.ai.AiAssistPanelJson;
import verbly.spring.domain.correction.ai.AiClientRouter;
import verbly.spring.domain.correction.ai.AiJsonSchemaKind;
import verbly.spring.domain.correction.dto.response.CorrectionAiAssistResponseDTO;
import verbly.spring.domain.correction.entity.Correction;
import verbly.spring.domain.correction.exception.CorrectionHandler;
import verbly.spring.domain.correction.repository.CorrectionRepository;
import verbly.spring.domain.library.dto.response.LibraryAiDTO;
import verbly.spring.domain.library.service.LibraryAiService;
import verbly.spring.domain.library.service.LibraryAutoFillService;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.security.utils.SecurityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CorrectionAiAssistService {

    private static final String SYSTEM_PROMPT = """
        You are an English writing assistant.
        Generate a helper panel for the user.
        Return ONLY valid JSON (no markdown, no extra text).
        
        JSON schema:
        {
          "toneManner": {
            "grade": "GOOD|OK|BAD",
            "casualToFormal": 0-100,
            "commentKo": "Korean one-sentence comment"
          },
          "suggestions": [
            { "original": "...", "revised": "...", "reasonKo": "..." }
          ],
          "recommendedPhrases": ["...", "..."]
        }
        
        Rules:
        - suggestions should be up to 3 items.
        - reasonKo and commentKo MUST be Korean.
        - revised must keep meaning but improve grammar/clarity/tone.
        - recommendedPhrases: short useful phrases/snippets (chips).
        - recommendedPhrases MUST be complete (no truncation).
        
        IMPORTANT — reasonKo requirements:
        - Do NOT write vague explanations like:
          "자연스럽게 수정했습니다"
          "정상적으로 수정했습니다"
          "문장을 다듬었습니다"
        - You MUST explain specifically:
          1) what grammar rule was changed (e.g., past tense, article usage, preposition, word choice)
          2) why it is incorrect or unnatural
          3) what structure is now used
        - Mention the exact expressions that were changed.
        - Example of GOOD reasonKo:
          "과거 시제 'didn't know'를 사용하고, 간접 의문문에서는 어순이 'where the station was'처럼 평서문 형태가 되어야 하므로 수정했습니다. 또한 'ask to' 대신 목적어가 있는 경우 'ask + 사람 + to' 구조를 사용해야 합니다."
        - Minimum 15 Korean characters.
        - Be concrete and educational.
        
        recommendedPhrases requirements (IMPORTANT):
        - recommendedPhrases are NOT full sentences.
        - Output short phrase chunks/idiomatic expressions like "Best regards", "Looking forward to", "As soon as possible".
        - Each item must be 1–4 words (max 5 words).
        - Do NOT include subject + verb full sentence forms.
        - Do NOT include personal pronouns starting a full sentence unless it's a fixed phrase.
        - No ellipsis "...", "…", "~".
        - No ending punctuation like "." "!" "?"
        - Keep them practical and reusable in daily writing.
        
        recommendedPhrases Examples:
        Bad: "I will submit it by Friday"
        Bad: "The deadline is approaching"
        Good: "By Friday"
        Good: "In progress"
        Good: "Ahead of schedule"
        Good: "Meeting canceled"
        Good: "Looking forward to"
        Good: "Best regards"
        """;

    private final CorrectionRepository correctionRepository;
    private final AiClientRouter aiClientRouter;
    private final ObjectMapper objectMapper;
    private final LibraryAiService libraryAiService;
    private final LibraryAutoFillService libraryAutoFillService;

    public CorrectionAiAssistResponseDTO.Result runAiAssist(Long correctionId) {
        Correction correction = correctionRepository.findById(correctionId)
                .orElseThrow(() -> new CorrectionHandler(ErrorStatus.CORRECTION_NOT_FOUND));

        validateAuthor(correction);

        String content = correction.getPost().getContent();
        if (content == null || content.isBlank()) {
            throw new CorrectionHandler(ErrorStatus.CORRECTION_NOT_VALIDATE);
        }

        correction.markAiAssistant();

        String userPrompt = buildUserPrompt(content);

        String json = aiClientRouter.current().generateJson(
                SYSTEM_PROMPT,
                userPrompt,
                AiJsonSchemaKind.AI_ASSIST_PANEL
        );

        AiAssistPanelJson parsed = parseJsonOrThrow(json);

        try {
            Long userId = SecurityUtils.getCurrentUserId();
            autofillLibraryFromSuggestions(userId, correctionId, parsed);
        } catch (Exception e) {
            // 라이브러리 저장 실패시에도 AI 도우미는 정상 반환
            log.warn("Library autofill failed. correctionId={}", correctionId, e);
        }

        return toResponse(parsed);
    }

    private void autofillLibraryFromSuggestions(Long userId, Long correctionId, AiAssistPanelJson parsed) {
        if (parsed == null || parsed.getSuggestions() == null || parsed.getSuggestions().isEmpty()) return;

        for (AiAssistPanelJson.Suggestion s : parsed.getSuggestions()) {
            if (s == null) continue;

            String original = safeTrim(s.getOriginal());
            String revised = safeTrim(s.getRevised());

            if (original.isBlank() || revised.isBlank()) continue;
            if (Objects.equals(original, revised)) continue;

            LibraryAiDTO learning = libraryAiService.createLearningPoint(original, revised);

            List<LibraryAutoFillService.ExampleInput> inputs = mapToLibraryInputs(learning);

            if (!inputs.isEmpty()) {
                libraryAutoFillService.fillFromCorrection(userId, correctionId, inputs);
            }
        }
    }

    private List<LibraryAutoFillService.ExampleInput> mapToLibraryInputs(LibraryAiDTO dto) {
        if (dto == null || dto.getPoints() == null) return List.of();

        List<LibraryAutoFillService.ExampleInput> result = new ArrayList<>();

        for (LibraryAiDTO.LearningPoint p : dto.getPoints()) {
            if (p == null) continue;

            String lemma = safeTrim(p.getRootExpression());
            if (lemma.isBlank()) continue;

            String meaningKo = safeTrimNullable(p.getMeaningKo());

            List<LibraryAutoFillService.ExamplePairInput> pairs = new ArrayList<>();

            if (p.getExamples() != null) {
                for (LibraryAiDTO.Example ex : p.getExamples()) {
                    if (ex == null) continue;

                    String exEn = safeTrim(ex.getSentence());
                    if (exEn.isBlank()) continue;

                    String exKo = safeTrimNullable(ex.getTranslationKo());

                    pairs.add(new LibraryAutoFillService.ExamplePairInput(exEn, exKo));
                }
            }

            result.add(new LibraryAutoFillService.ExampleInput(lemma, meaningKo, pairs));
        }

        return result;
    }


    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private String safeTrimNullable(String s) {
        String t = safeTrim(s);
        return t.isBlank() ? null : t;
    }

    private String buildUserPrompt(String content) {
        return """
                TEXT:
                %s

                Output the helper panel JSON.
                """.formatted(content);
    }

    private void validateAuthor(Correction correction) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (correction.getPost() == null
                || correction.getPost().getAuthor() == null
                || !correction.getPost().getAuthor().getId().equals(currentUserId)) {
            throw new CorrectionHandler(ErrorStatus.CORRECTION_ACCESS_DENIED);
        }
    }

    private AiAssistPanelJson parseJsonOrThrow(String json) {
        try {
            return objectMapper.readValue(json, AiAssistPanelJson.class);
        } catch (Exception e) {
            throw new CorrectionHandler(ErrorStatus.AI_RESPONSE_INVALID);
        }
    }

    private CorrectionAiAssistResponseDTO.Result toResponse(AiAssistPanelJson parsed) {
        CorrectionAiAssistResponseDTO.ToneManner tm =
                parsed.getToneManner() == null ? null
                        : CorrectionAiAssistResponseDTO.ToneManner.builder()
                        .grade(parsed.getToneManner().getGrade())
                        .casualToFormal(parsed.getToneManner().getCasualToFormal())
                        .commentKo(parsed.getToneManner().getCommentKo())
                        .build();

        List<CorrectionAiAssistResponseDTO.Suggestion> suggestions =
                parsed.getSuggestions() == null ? List.of()
                        : parsed.getSuggestions().stream()
                        .map(s -> CorrectionAiAssistResponseDTO.Suggestion.builder()
                                .original(s.getOriginal())
                                .revised(s.getRevised())
                                .reasonKo(s.getReasonKo())
                                .build())
                        .toList();

        List<String> phrases = parsed.getRecommendedPhrases() == null ? List.of()
                : parsed.getRecommendedPhrases().stream()
                .filter(p -> p != null && !p.isBlank())
                .map(this::sanitizeRecommendedPhrase)
                .filter(p -> !p.isBlank())
                .distinct()
                .toList();

        return CorrectionAiAssistResponseDTO.Result.builder()
                .toneManner(tm)
                .suggestions(suggestions)
                .suggestionCount(suggestions.size())
                .recommendedPhrases(phrases)
                .build();
    }

    private String sanitizeRecommendedPhrase(String p) {
        // 말줄임표,생략 기호 제거
        String s = p.replace("...", "")
                .replace("…", "")
                .replace("~", "")
                .trim();

        // 문장 끝 기호 제거
        while (s.endsWith(".") || s.endsWith(",") || s.endsWith(";") || s.endsWith(":")) {
            s = s.substring(0, s.length() - 1).trim();
        }
        return s;
    }
}
