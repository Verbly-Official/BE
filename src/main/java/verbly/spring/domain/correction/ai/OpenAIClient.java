package verbly.spring.domain.correction.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import verbly.spring.domain.correction.exception.CorrectionHandler;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.common.dto.openAI.OpenAIMessage;
import verbly.spring.global.common.dto.openAI.OpenAIRequestDTO;
import verbly.spring.global.common.dto.openAI.OpenAIResponseDTO;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OpenAIClient {

    public enum JsonSchemaKind {
        AI_ASSIST_PANEL,
        WORD_EDITS
    }

    private final RestTemplate openAiRestTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openai.api-url}")
    private String apiUrl;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    public OpenAIResponseDTO getChatCompletionWithJsonSchema(
            String systemPrompt,
            String userPrompt,
            JsonSchemaKind kind
    ) {
        OpenAIRequestDTO req = buildRequest(systemPrompt, userPrompt, kind);

        ResponseEntity<OpenAIResponseDTO> res = openAiRestTemplate.postForEntity(
                apiUrl,
                req,
                OpenAIResponseDTO.class
        );

        if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
            throw new CorrectionHandler(ErrorStatus.OPENAI_API_CALL_FAILED);
        }
        return res.getBody();
    }

    private OpenAIRequestDTO buildRequest(String systemPrompt, String userPrompt, JsonSchemaKind kind) {
        OpenAIMessage system = new OpenAIMessage("system", systemPrompt);
        OpenAIMessage user = new OpenAIMessage("user", userPrompt);

        Map<String, Object> responseFormat = switch (kind) {
            case AI_ASSIST_PANEL -> buildAiAssistPanelJsonSchema();
            case WORD_EDITS -> buildWordEditsJsonSchema();
        };

        return new OpenAIRequestDTO(
                model,
                List.of(system, user),
                0.0,
                responseFormat
        );
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
