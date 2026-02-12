package verbly.spring.domain.correction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.correction.ai.AiAssistPanelJson;
import verbly.spring.domain.correction.ai.AiClientRouter;
import verbly.spring.domain.correction.ai.AiJsonSchemaKind;
import verbly.spring.domain.correction.dto.response.CorrectionAiAssistResponseDTO;
import verbly.spring.domain.correction.entity.Correction;
import verbly.spring.domain.correction.exception.CorrectionHandler;
import verbly.spring.domain.correction.repository.CorrectionRepository;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.security.utils.SecurityUtils;

import java.util.List;

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

            recommendedPhrases requirements (IMPORTANT):
            - recommendedPhrases are NOT full sentences.
            - Output short phrase chunks/idiomatic expressions like "Best regards", "Looking forward to", "As soon as possible".
            - Each item must be 1–4 words (max 5 words).
            - Do NOT include subject + verb full sentence forms (e.g., "I will submit it by Friday" is forbidden).
            - Do NOT include personal pronouns starting a full sentence ("I", "We", "She", "He", "They") unless it's a fixed phrase.
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
        return toResponse(parsed);
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
