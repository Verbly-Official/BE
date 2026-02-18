package verbly.spring.domain.correction.ai;

import lombok.RequiredArgsConstructor;
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
}
