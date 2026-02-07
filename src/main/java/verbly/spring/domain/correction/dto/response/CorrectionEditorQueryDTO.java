package verbly.spring.domain.correction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import verbly.spring.domain.correction.enums.CorrectorType;
import verbly.spring.domain.post.entity.Post;
import verbly.spring.domain.post.enums.PostStatus;

import java.time.LocalDateTime;

public class CorrectionEditorQueryDTO {
    @Getter
    @AllArgsConstructor
    public static class CorrectionBaseRow {
        private Long correctionId;
        private Long postId;
        private PostStatus status;
        private String postContent;
    }

    @Getter
    @AllArgsConstructor
    public static class WordRow {
        private Long wordId;
        private Integer sentenceIdx;
        private Integer startIdx;
        private Integer endIdx;
        private String originalText;
        private String correctedText;
    }

    @Getter
    @AllArgsConstructor
    public static class FeedbackRow {
        private Long feedbackId;
        private Integer sentenceIdx;
        private String correctorName;
        private CorrectorType correctorType;
        private String content;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
