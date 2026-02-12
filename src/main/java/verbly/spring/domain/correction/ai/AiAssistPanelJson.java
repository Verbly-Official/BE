package verbly.spring.domain.correction.ai;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = ANY)
public class AiAssistPanelJson {
    private ToneManner toneManner;
    private List<Suggestion> suggestions;
    private List<String> recommendedPhrases;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = ANY)
    public static class ToneManner {
        private String grade;
        private Integer casualToFormal;
        private String commentKo;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = ANY)
    public static class Suggestion {
        private String original;
        private String revised;
        private String reasonKo;
    }
}
