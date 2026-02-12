package verbly.spring.domain.correction.ai;

public interface AiProviderClient {
    AiProvider provider();

    String generateJson(
            String systemPrompt,
            String userPrompt,
            AiJsonSchemaKind kind
    );
}
