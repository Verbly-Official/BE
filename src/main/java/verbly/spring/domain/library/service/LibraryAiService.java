package verbly.spring.domain.library.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import verbly.spring.domain.correction.ai.AiJsonSchemaKind;
import verbly.spring.domain.correction.ai.GeminiProviderClient;
import verbly.spring.domain.library.dto.response.LibraryAiDTO;

@Service
@RequiredArgsConstructor
@Slf4j
public class LibraryAiService {

    private final GeminiProviderClient geminiClient;
    private final ObjectMapper objectMapper;

    public LibraryAiDTO createLearningPoint(String original, String revised) {
        String systemPrompt = "You are an English grammar expert.";

        String userPrompt = String.format("""
            Compare these two sentences and identify **ALL** grammatical and lexical changes.
            
            Original: "%s"
            Revised: "%s"
            
            // ✅ 예문에 한글 번역을 포함하도록 지시사항 추가
            For EACH change found, provide the root expression, Korean meaning, and examples (including Korean translations).
            Return the result as a JSON object containing a list of 'points'.
            """, original, revised);

        String jsonResult = geminiClient.generateJson(
                systemPrompt,
                userPrompt,
                AiJsonSchemaKind.LEARNING_POINT
        );

        try {
            return objectMapper.readValue(jsonResult, LibraryAiDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("AI 응답 파싱 실패", e);
        }
    }
}
