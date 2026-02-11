package verbly.spring.domain.correction.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class CorrectionAiAssistResponseDTO {
    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Result {
        private final ToneManner toneManner;
        private final List<Suggestion> suggestions;
        private final List<String> recommendedPhrases;
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ToneManner {
        private final String grade;          // GOOD / OK / BAD
        private final Integer casualToFormal; // 0~100
        private final String commentKo;      // 한줄 평가
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Suggestion {
        private final String original; // 빨간 원문
        private final String revised;  // 초록 추천문
        private final String reasonKo; // 한국어 이유
    }
}
