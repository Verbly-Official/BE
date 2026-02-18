package verbly.spring.domain.library.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@NoArgsConstructor
@ToString
public class LibraryAiDTO {
    @JsonProperty("points")
    private List<LearningPoint> points;

    @Getter
    @NoArgsConstructor
    @ToString
    public static class LearningPoint {
        @JsonProperty("errorPart")
        private String errorPart;

        @JsonProperty("correctPart")
        private String correctPart;

        @JsonProperty("rootExpression")
        private String rootExpression;

        @JsonProperty("meaningKo")
        private String meaningKo;

        @JsonProperty("examples")
        private List<String> examples;
    }
}
