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

            Rules:
            - commentKo and reasonKo MUST be Korean.
            - revised keeps meaning but improves grammar/clarity/tone.
            - suggestions up to 3 items.
            - recommendedPhrases are short chips.
            """;

    private final CorrectionRepository correctionRepository;
    private final UserRepository userRepository;
    private final OpenAIClient openAIClient;
    private final ObjectMapper objectMapper;

    @Value("${app.ai-user-id}")
    private Long aiUserId;

    public CorrectionAiAssistResponseDTO.Result runAiAssist(Long correctionId) {
        Correction correction = correctionRepository.findById(correctionId)
                .orElseThrow(() -> new CorrectionHandler(ErrorStatus.CORRECTION_NOT_FOUND));

        validateAuthor(correction);

        // PENDING만 AI 첨삭하기 가능
        if (correction.getPost().getStatus() != PostStatus.PENDING) {
            throw new CorrectionHandler(ErrorStatus.CORRECTION_FIRST_ACTION_ONLY_PENDING);
        }

        if (correction.getCorrectorType() == CorrectorType.NATIVE_SPEAKER) {
            throw new CorrectionHandler(ErrorStatus.CORRECTION_EDIT_ONLY_IN_PROGRESS);
        }

        applyAiCorrector(correction);

        String content = correction.getPost().getContent();
        if (content == null || content.isBlank()) {
            throw new CorrectionHandler(ErrorStatus.CORRECTION_NOT_VALIDATE);
        }

        String userPrompt = """
                TEXT:
                %s
                """.formatted(content);

        OpenAIResponseDTO res = openAIClient.getAiHelperPanel(SYSTEM_PROMPT, userPrompt);

        String json = extractContentOrThrow(res);

        AiAssistPanelJson parsed = parseJsonOrThrow(json);

        return toResponse(parsed);
    }

    private void validateAuthor(Correction correction) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (correction.getPost() == null
                || correction.getPost().getAuthor() == null
                || !correction.getPost().getAuthor().getId().equals(currentUserId)) {
            throw new CorrectionHandler(ErrorStatus.CORRECTION_ACCESS_DENIED);
        }
    }

    private void applyAiCorrector(Correction correction) {
        User aiUser = userRepository.findById(aiUserId)
                .orElseThrow(() -> new CorrectionHandler(ErrorStatus.USER_NOT_FOUND));

        correction.assignCorrector(aiUser);
        correction.changeCorrectorType(CorrectorType.AI_ASSISTANT);
    }

    private AiAssistPanelJson parseJsonOrThrow(String json) {
        try {
            return objectMapper.readValue(json, AiAssistPanelJson.class);
        } catch (Exception e) {
            throw new CorrectionHandler(ErrorStatus.OPENAI_RESPONSE_INVALID);
        }
    }

    private CorrectionAiAssistResponseDTO.Result toResponse(AiAssistPanelJson parsed) {
        var tm = CorrectionAiAssistResponseDTO.ToneManner.builder()
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

        List<String> phrases =
                parsed.getRecommendedPhrases() == null ? List.of()
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
