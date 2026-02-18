package verbly.spring.global.infrastructure.gemini.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GeminiGenerateResponseDTO {
    private List<Candidate> candidates;

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Candidate {
        private Content content;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Content {
        private List<Part> parts;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Part {
        private String text;
    }
}
