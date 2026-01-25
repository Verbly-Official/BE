package verbly.spring.domain.library.converter;

import verbly.spring.domain.library.dto.response.LibraryResponseDTO;
import verbly.spring.domain.library.entity.LibraryItem;
import verbly.spring.domain.library.entity.LibraryItemExample;
import verbly.spring.domain.library.entity.LibraryItemSource;

import java.util.List;

/**
 * 라이브러리 도메인 Entity -> DTO 변환 전용 클래스
 * - Controller/Service에서 매핑 로직이 흩어지지 않도록 한 곳으로 모읍니다.
 */
public class LibraryConverter {

    private LibraryConverter() {}

    public static LibraryResponseDTO.CreateItemResponse toCreateItemResponse(LibraryItem item) {
        return new LibraryResponseDTO.CreateItemResponse(item.getId());
    }

    public static LibraryResponseDTO.ItemSummary toItemSummary(LibraryItem li) {
        return new LibraryResponseDTO.ItemSummary(
                li.getId(),
                li.getPhrase(),
                li.getMeaningKo(),
                li.getMeaningEn(),
                li.isStarred(),
                li.getStatus().name(),
                li.getUpdatedAt()
        );
    }

    public static LibraryResponseDTO.Source toSource(LibraryItemSource s) {
        return new LibraryResponseDTO.Source(
                s.getId(),
                s.getPostId(),
                s.getCorrectionId(),
                s.getFeedbackId(),
                s.getCorrectionWordId(),
                s.getSentenceIndex(),
                s.getTokenStart(),
                s.getTokenEnd(),
                s.getOriginalSegment(),
                s.getSuggestionSegment(),
                s.getSourceStatus().name(),
                s.getCreatedAt()
        );
    }

    public static LibraryResponseDTO.Example toExample(LibraryItemExample e) {
        return new LibraryResponseDTO.Example(
                e.getId(),
                e.getExampleEn(),
                e.getExampleKo(),
                e.getSource().name(),
                e.getCreatedAt()
        );
    }

    public static LibraryResponseDTO.ItemDetail toItemDetail(
            LibraryItem item,
            List<LibraryItemSource> sources,
            List<LibraryItemExample> examples
    ) {
        return new LibraryResponseDTO.ItemDetail(
                item.getId(),
                item.getPhrase(),
                item.getMeaningKo(),
                item.getMeaningEn(),
                item.isStarred(),
                item.getStatus().name(),
                sources.stream().map(LibraryConverter::toSource).toList(),
                examples.stream().map(LibraryConverter::toExample).toList(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }

    /**
     * 중복 방지용 정규화(공백/대소문자 통일)
     */
    public static String normalizePhrase(String phrase) {
        return phrase == null ? "" : phrase.trim().toLowerCase().replaceAll("\\s+", " ");
    }
}
