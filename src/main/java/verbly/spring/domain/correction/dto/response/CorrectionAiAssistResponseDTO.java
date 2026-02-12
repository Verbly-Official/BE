package verbly.spring.domain.correction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class CorrectionAiAssistResponseDTO {
    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Schema(description = "AI 첨삭 결과")
    public static class Result {
        @Schema(description = "톤/매너 분석 결과")
        private final ToneManner toneManner;

        @Schema(description = "문장 수정 제안 개수", example = "3")
        private Integer suggestionCount;

        @Schema(description = "문장 수정 제안 목록 (최대 3개)")
        private final List<Suggestion> suggestions;

        @Schema(description = "추천 표현 목록 (완전한 문장/구 형태, 말줄임표 없음)")
        private final List<String> recommendedPhrases;
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Schema(description = "Tone & Manner 분석 결과")
    public static class ToneManner {
        @Schema(description = "글의 전체적인 완성도 평가 (GOOD | OK | BAD)", example = "BAD")
        private final String grade;          // GOOD / OK / BAD

        @Schema(description = "문장의 격식 수준 (0=매우 캐주얼, 100=매우 포멀)", example = "50")
        private final Integer casualToFormal; // 0~100

        @Schema(description = "한국어 한 줄 총평", example = "문법 오류가 많아 전반적인 글의 완성도가 낮습니다.")
        private final String commentKo;      // 한줄 평가
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Suggestion {
        @Schema(description = "사용자가 작성한 원문 문장", example = "She don’t like coffee.")
        private final String original; // 빨간 원문

        @Schema(description = "AI가 수정한 문장", example = "She doesn’t like coffee.")
        private final String revised;  // 초록 추천문

        @Schema(description = "수정 이유 (한국어)", example = "3인칭 단수이므로 doesn't를 사용해야 합니다.")
        private final String reasonKo; // 한국어 이유
    }
}
