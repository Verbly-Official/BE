package verbly.spring.domain.correction.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import verbly.spring.domain.correction.exception.CorrectionHandler;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.infrastructure.openAI.dto.OpenAIMessage;
import verbly.spring.global.infrastructure.openAI.dto.OpenAIRequestDTO;
import verbly.spring.global.infrastructure.openAI.dto.OpenAIResponseDTO;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.provider", havingValue = "openai")
public class OpenAiProviderClient implements AiProviderClient{

    private final RestTemplate openAiRestTemplate;

    @Value("${openai.api-url}")
    private String apiUrl;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    @Override
    public AiProvider provider() {
        return AiProvider.OPENAI;
    }

    @Override
    public String generateJson(String systemPrompt, String userPrompt, AiJsonSchemaKind kind) {
        OpenAIRequestDTO req = buildRequest(systemPrompt, userPrompt, kind);

        ResponseEntity<OpenAIResponseDTO> res = openAiRestTemplate.postForEntity(
                apiUrl,
                req,
                OpenAIResponseDTO.class
        );

        if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
            throw new CorrectionHandler(ErrorStatus.AI_API_CALL_FAILED);
        }

        return extractContentOrThrow(res.getBody());
    }

    private OpenAIRequestDTO buildRequest(String systemPrompt, String userPrompt, AiJsonSchemaKind kind) {
        Map<String, Object> responseFormat = switch (kind) {
            case AI_ASSIST_PANEL -> buildAiAssistPanelJsonSchema();
            case WORD_EDITS -> buildWordEditsJsonSchema();
        };

        return new OpenAIRequestDTO(
                model,
                List.of(
                        new OpenAIMessage("system", systemPrompt),
                        new OpenAIMessage("user", userPrompt)
                ),
                0.0,
                responseFormat
        );
    }

    private String extractContentOrThrow(OpenAIResponseDTO res) {
        if (res.getChoices() == null || res.getChoices().isEmpty()
                || res.getChoices().get(0).getMessage() == null) {
            throw new CorrectionHandler(ErrorStatus.AI_RESPONSE_INVALID);
        }
        String content = res.getChoices().get(0).getMessage().getContent();
        if (content == null || content.isBlank()) {
            throw new CorrectionHandler(ErrorStatus.AI_RESPONSE_INVALID);
        }
        return content;
    }

    private Map<String, Object> buildAiAssistPanelJsonSchema() {
        return Map.of(
                "type", "json_schema",
                "json_schema", Map.of(
                        "name", "ai_assist_panel",
                        "strict", true,
                        "schema", Map.of(
                                "type", "object",
                                "additionalProperties", false,
                                "properties", Map.of(
                                        "toneManner", Map.of(
                                                "type", "object",
                                                "additionalProperties", false,
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
                                                        "additionalProperties", false,
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
                        )
                )
        );
    }

    private Map<String, Object> buildWordEditsJsonSchema() {
        return Map.of(
                "type", "json_schema",
                "json_schema", Map.of(
                        "name", "word_edits",
                        "strict", true,
                        "schema", Map.of(
                                "type", "object",
                                "additionalProperties", false,
                                "properties", Map.of(
                                        "edits", Map.of(
                                                "type", "array",
                                                "items", Map.of(
                                                        "type", "object",
                                                        "additionalProperties", false,
                                                        "properties", Map.of(
                                                                "wordId", Map.of("type", "integer"),
                                                                "correctedText", Map.of("type", "string")
                                                        ),
                                                        "required", List.of("wordId", "correctedText")
                                                )
                                        )
                                ),
                                "required", List.of("edits")
                        )
                )
        );
    }
}
