package verbly.spring.domain.library.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class LibraryResponseDTO {

    public record ItemSummary(
            Long id,
            String phrase,
            String meaningKo,
            String meaningEn,
            boolean starred,
            String status,
            LocalDateTime updatedAt
    ) {}

    public record PageResult<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean last
    ) {}

    public record Source(
            Long id,
            Long postId,
            Long correctionId,
            Long feedbackId,
            Long correctionWordId,
            Integer sentenceIndex,
            Integer tokenStart,
            Integer tokenEnd,
            String originalSegment,
            String suggestionSegment,
            String sourceStatus,
            LocalDateTime createdAt
    ) {}

    public record Example(
            Long id,
            String exampleEn,
            String exampleKo,
            String source,
            LocalDateTime createdAt
    ) {}

    public record ItemDetail(
            Long id,
            String phrase,
            String meaningKo,
            String meaningEn,
            boolean starred,
            String status,
            List<Source> sources,
            List<Example> examples,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    public record CreateItemResponse(Long id) {}
}
