package verbly.spring.domain.correction.tokenizer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WordToken {

    private final int sentenceIdx;   // 몇 번째 문장
    private final int startIdx;      // 문장 내 시작
    private final int endIdx;        // 문장 내 끝
    private final String text;       // 원문 단어
}
