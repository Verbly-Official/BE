package verbly.spring.domain.correction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.correction.ai.AiAssistPanelJson;
import verbly.spring.domain.correction.ai.OpenAIClient;
import verbly.spring.domain.correction.dto.response.CorrectionAiAssistResponseDTO;
import verbly.spring.domain.correction.entity.Correction;
import verbly.spring.domain.correction.enums.CorrectorType;
import verbly.spring.domain.correction.exception.CorrectionHandler;
import verbly.spring.domain.correction.repository.CorrectionRepository;
import verbly.spring.domain.post.enums.PostStatus;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.repository.UserRepository;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.common.dto.openAI.OpenAIResponseDTO;
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
            """;

    private final CorrectionRepository correctionRepository;
    private final OpenAIClient openAIClient;
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

        OpenAIResponseDTO res = openAIClient.getChatCompletionWithJsonSchema(
                SYSTEM_PROMPT,
                userPrompt,
                OpenAIClient.JsonSchemaKind.AI_ASSIST_PANEL
        );

        String json = extractContentOrThrow(res);
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
            throw new CorrectionHandler(ErrorStatus.OPENAI_RESPONSE_INVALID);
        }
    }

    private CorrectionAiAssistResponseDTO.Result toResponse(AiAssistPanelJson parsed) {
        CorrectionAiAssistResponseDTO.ToneManner tm = CorrectionAiAssistResponseDTO.ToneManner.builder()
                .grade(parsed.getToneManner() == null ? null : parsed.getToneManner().getGrade())
                .casualToFormal(parsed.getToneManner() == null ? null : parsed.getToneManner().getCasualToFormal())
                .commentKo(parsed.getToneManner() == null ? null : parsed.getToneManner().getCommentKo())
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
                .distinct()
                .toList();

        return CorrectionAiAssistResponseDTO.Result.builder()
                .toneManner(tm)
                .suggestions(suggestions)
                .recommendedPhrases(phrases)
                .build();
    }

    private String extractContentOrThrow(OpenAIResponseDTO res) {
        if (res == null || res.getChoices() == null || res.getChoices().isEmpty()
                || res.getChoices().get(0).getMessage() == null) {
            throw new CorrectionHandler(ErrorStatus.OPENAI_RESPONSE_INVALID);
        }
        String content = res.getChoices().get(0).getMessage().getContent();
        if (content == null || content.isBlank()) {
            throw new CorrectionHandler(ErrorStatus.OPENAI_RESPONSE_INVALID);
        }
        return content;
    }
}
