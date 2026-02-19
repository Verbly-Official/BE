package verbly.spring.domain.correction.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import verbly.spring.domain.correction.exception.CorrectionHandler;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.infrastructure.gemini.dto.GeminiGenerateRequestDTO;
import verbly.spring.global.infrastructure.gemini.dto.GeminiGenerateResponseDTO;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.provider", havingValue = "gemini", matchIfMissing = true)
@Slf4j
public class GeminiProviderClient implements AiProviderClient{

    private final RestTemplate geminiRestTemplate;

    @Value("${gemini.api-url}")
    private String apiUrl;

    @Value("${gemini.model}")
    private String model;

    @Override
    public AiProvider provider() {
        return AiProvider.GEMINI;
    }

    @Override
    public String generateJson(String systemPrompt, String userPrompt, AiJsonSchemaKind kind) {
        Map<String, Object> schemaOnly = switch (kind) {
            case AI_ASSIST_PANEL -> buildAiAssistPanelSchemaOnly();
            case WORD_EDITS -> buildWordEditsSchemaOnly();
            case LEARNING_POINT -> buildLearningPointSchemaOnly();
        };

        String url = apiUrl + "/models/" + model + ":generateContent";

        GeminiGenerateRequestDTO req = new GeminiGenerateRequestDTO(
                new GeminiGenerateRequestDTO.SystemInstruction(
                        List.of(new GeminiGenerateRequestDTO.Part(systemPrompt))
                ),
                List.of(
                        new GeminiGenerateRequestDTO.Content(
                                "user",
                                List.of(new GeminiGenerateRequestDTO.Part(userPrompt))
                        )
                ),
                new GeminiGenerateRequestDTO.GenerationConfig(
                        0.0,
                        "application/json",
                        schemaOnly
                )
        );

        ResponseEntity<GeminiGenerateResponseDTO> res =
                geminiRestTemplate.postForEntity(url, req, GeminiGenerateResponseDTO.class);

        if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
            throw new CorrectionHandler(ErrorStatus.AI_API_CALL_FAILED);
        }

        return extractTextOrThrow(res.getBody());
    }

    private String extractTextOrThrow(GeminiGenerateResponseDTO res) {
        if (res.getCandidates() == null || res.getCandidates().isEmpty()
                || res.getCandidates().get(0).getContent() == null
                || res.getCandidates().get(0).getContent().getParts() == null
                || res.getCandidates().get(0).getContent().getParts().isEmpty()) {
            throw new CorrectionHandler(ErrorStatus.AI_RESPONSE_INVALID);
        }
        String text = res.getCandidates().get(0).getContent().getParts().get(0).getText();

        // ✅ [추가] 이 로그를 추가해야 범인을 잡습니다!
        log.info("🤖 제미나이 원본 응답: " + text);
        // (Slf4j가 있으면 log.info("🤖 ... {}", text); 로 쓰세요)
        if (text == null || text.isBlank()) {
            throw new CorrectionHandler(ErrorStatus.AI_RESPONSE_INVALID);
        }
        return text;
    }

    private Map<String, Object> buildAiAssistPanelSchemaOnly() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "toneManner", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "grade", Map.of("type", "string", "enum", List.of("GOOD", "OK", "BAD")),
                                        "casualToFormal", Map.of("type", "integer"),
                                        "commentKo", Map.of("type", "string")
                                ),
                                "required", List.of("grade", "casualToFormal", "commentKo")
                        ),
                        "suggestions", Map.of(
                                "type", "array",
                                "items", Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "original", Map.of("type", "string"),
                                                "revised", Map.of("type", "string"),
                                                "reasonKo", Map.of("type", "string")
                                        ),
                                        "required", List.of("original", "revised", "reasonKo")
                                )
                        ),
                        "recommendedPhrases", Map.of(
                                "type", "array",
                                "items", Map.of("type", "string")
                        )
                ),
                "required", List.of("toneManner", "suggestions", "recommendedPhrases")
        );
    }

    private Map<String, Object> buildWordEditsSchemaOnly() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "edits", Map.of(
                                "type", "array",
                                "items", Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "wordId", Map.of("type", "integer"),
                                                "correctedText", Map.of("type", "string")
                                        ),
                                        "required", List.of("wordId", "correctedText")
                                )
                        )
                ),
                "required", List.of("edits")
        );
    }

    private Map<String, Object> buildLearningPointSchemaOnly() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "points", Map.of(
                                "type", "array",
                                "description", "A list of all grammatical corrections found in the sentence pair.",
                                "items", Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "errorPart", Map.of("type", "string", "description", "The incorrect part in Original."),
                                                "correctPart", Map.of("type", "string", "description", "The corrected part in Revised."),
                                                "rootExpression", Map.of("type", "string", "description", "Base form or idiom."),
                                                "meaningKo", Map.of("type", "string", "description", "Korean meaning."),
                                                "examples", Map.of(
                                                        "type", "array",
                                                        "description", "3 example sentences with their Korean translations.",
                                                        "items", Map.of(
                                                                "type", "object",
                                                                "properties", Map.of(
                                                                        "sentence", Map.of("type", "string", "description", "English example sentence."),
                                                                        "translationKo", Map.of("type", "string", "description", "Korean translation of the sentence.")
                                                                ),
                                                                "required", List.of("sentence", "translationKo")
                                                        )
                                                )
                                        ),
                                        "required", List.of("errorPart", "correctPart", "rootExpression", "meaningKo", "examples")
                                )
                        )
                ),
                "required", List.of("points")
        );
    }
}
