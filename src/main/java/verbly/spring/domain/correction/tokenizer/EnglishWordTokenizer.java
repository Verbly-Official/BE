package verbly.spring.domain.correction.tokenizer;

import org.springframework.stereotype.Component;
import verbly.spring.domain.correction.converter.CorrectionEditorConverter;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class EnglishWordTokenizer {
    public List<WordToken> tokenize(String content) {
        if (content == null || content.isBlank()) {
            return List.of();
        }

        List<WordToken> result = new ArrayList<>();

        List<String> sentences = CorrectionEditorConverter.splitSentences(content);


        for (int sentenceIdx = 0; sentenceIdx < sentences.size(); sentenceIdx++) {
            String sentence = sentences.get(sentenceIdx);

            tokenizeSentence(sentence, sentenceIdx, result);
        }

        return result;
    }

    private void tokenizeSentence(String sentence, int sentenceIdx, List<WordToken> out) {
        BreakIterator it = BreakIterator.getWordInstance(Locale.ENGLISH);
        it.setText(sentence);

        int start = it.first();
        for (int end = it.next(); end != BreakIterator.DONE; start = end, end = it.next()) {
            String raw = sentence.substring(start, end);

            if (!looksLikeWord(raw)) continue;

            out.add(new WordToken(
                    sentenceIdx,
                    start,
                    end,
                    raw
            ));
        }
    }

    private boolean looksLikeWord(String s) {
        boolean hasAlphaNum = false;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (Character.isLetterOrDigit(c)) hasAlphaNum = true;
            else if (c == '\'' || c == '-') continue;
            else return false;
        }
        return hasAlphaNum;
    }
}
