package verbly.spring.global.infrastructure.gemini.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class GeminiGenerateRequestDTO {
    private SystemInstruction systemInstruction;
    private List<Content> contents;
    private GenerationConfig generationConfig;

    @Getter @NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor
    public static class SystemInstruction {
        private List<Part> parts;
    }

    @Getter @NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor
    public static class Content {
        private String role;
        private List<Part> parts;
    }

    @Getter @NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor
    public static class Part {
        private String text;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class GenerationConfig {
        private Double temperature;

        @JsonProperty("response_mime_type")
        private String responseMimeType;

        @JsonProperty("response_schema")
        private Map<String, Object> responseSchema;
    }
}
