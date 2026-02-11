package verbly.spring.domain.correction.ai;

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
    private final RestTemplate openAiRestTemplate;

    @Value("${openai.api-url}")
    private String apiUrl;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    public OpenAIResponseDTO getAiHelperPanel(String systemPrompt, String userPrompt) {
        OpenAIRequestDTO req = buildPanelRequest(systemPrompt, userPrompt);

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

    private OpenAIRequestDTO buildPanelRequest(String systemPrompt, String userPrompt) {
        OpenAIMessage system = new OpenAIMessage("system", systemPrompt);
        OpenAIMessage user = new OpenAIMessage("user", userPrompt);

        return new OpenAIRequestDTO(
                model,
                List.of(system, user),
                0.2,
                buildPanelJsonSchema()
        );
    }

    private Map<String, Object> buildPanelJsonSchema() {
        return Map.of(
                "type", "json_schema",
                "json_schema", Map.of(
                        "name", "ai_helper_panel",
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
                                                        "casualToFormal", Map.of("type", "integer", "minimum", 0, "maximum", 100),
                                                        "commentKo", Map.of("type", "string")
                                                ),
                                                "required", List.of("grade", "casualToFormal", "commentKo")
                                        ),
                                        "suggestions", Map.of(
                                                "type", "array",
                                                "maxItems", 3,
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
}
