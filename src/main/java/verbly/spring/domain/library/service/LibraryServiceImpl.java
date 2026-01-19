package verbly.spring.domain.library.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.library.dto.request.LibraryRequestDTO;
import verbly.spring.domain.library.dto.response.LibraryResponseDTO;
import verbly.spring.domain.library.entity.LibraryItem;
import verbly.spring.domain.library.entity.LibraryItemExample;
import verbly.spring.domain.library.entity.LibraryItemSource;
import verbly.spring.domain.library.enums.LibraryItemStatus;
import verbly.spring.domain.library.exception.LibraryErrorStatus;
import verbly.spring.domain.library.exception.LibraryException;
import verbly.spring.domain.library.repository.LibraryItemExampleRepository;
import verbly.spring.domain.library.repository.LibraryItemRepository;
import verbly.spring.domain.library.repository.LibraryItemSourceRepository;
import verbly.spring.domain.library.validator.LibraryValidator;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LibraryServiceImpl implements LibraryService {

    private final LibraryItemRepository libraryItemRepository;
    private final LibraryItemSourceRepository libraryItemSourceRepository;
    private final LibraryItemExampleRepository libraryItemExampleRepository;
    private final LibraryValidator libraryValidator;

    @Override
    @Transactional
    public LibraryResponseDTO.CreateItemResponse createItem(Long userId, LibraryRequestDTO.CreateItemRequest req) {
        String phraseNorm = normalize(req.phrase());

        // 중복 체크
        if (libraryItemRepository.findByUserIdAndPhraseNorm(userId, phraseNorm).isPresent()) {
            throw new LibraryException(LibraryErrorStatus.LIBRARY_ITEM_DUPLICATE);
        }

        LibraryItem item = LibraryItem.of(userId, req.phrase(), phraseNorm, req.meaningKo(), req.meaningEn());
        LibraryItem saved = libraryItemRepository.save(item);

        return new LibraryResponseDTO.CreateItemResponse(saved.getId());
    }

    @Override
    public Page<LibraryResponseDTO.ItemSummary> listItems(Long userId, String q, Boolean starred, Pageable pageable) {

        Page<LibraryItem> page;

        // 검색옵션별 사용이 됨
        if (q == null || q.isBlank()) {
            if (starred == null) {
                page = libraryItemRepository.findByUserIdAndStatus(userId, LibraryItemStatus.ACTIVE, pageable);
            } else {
                page = libraryItemRepository.findByUserIdAndStatusAndStarred(userId, LibraryItemStatus.ACTIVE, starred, pageable);
            }
        } else {
            if (starred == null) {
                page = libraryItemRepository.findByUserIdAndStatusAndPhraseContaining(userId, LibraryItemStatus.ACTIVE, q, pageable);
            } else {
                page = libraryItemRepository.findByUserIdAndStatusAndStarredAndPhraseContaining(userId, LibraryItemStatus.ACTIVE, starred, q, pageable);
            }
        }

        return page.map(this::toSummary);
    }

    @Override
    public LibraryResponseDTO.ItemDetail getItemDetail(Long userId, Long itemId) {
        LibraryItem item = libraryValidator.validateOwnedItem(userId, itemId);

        List<LibraryItemSource> sources = libraryItemSourceRepository.findByLibraryItem_Id(itemId);
        List<LibraryItemExample> examples = libraryItemExampleRepository.findByLibraryItem_Id(itemId);

        return new LibraryResponseDTO.ItemDetail(
                item.getId(),
                item.getPhrase(),
                item.getMeaningKo(),
                item.getMeaningEn(),
                item.isStarred(),
                item.getStatus().name(),
                sources.stream().map(this::toSource).toList(),
                examples.stream().map(this::toExample).toList(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }

    @Override
    @Transactional
    public void updateItem(Long userId, Long itemId, LibraryRequestDTO.UpdateItemRequest req) {
        LibraryItem item = libraryValidator.validateOwnedItem(userId, itemId);

        if (req.starred() != null) {
            item.toggleStar(req.starred());
        }

        libraryItemRepository.save(item);
    }
//    소프트 딜리트 감안해야함 (active 표시만 바꿔주는거 삭제해도 실제 db에 남아있음.)
    @Override
    @Transactional
    public void deleteItem(Long userId, Long itemId) {
        LibraryItem item = libraryValidator.validateOwnedItem(userId, itemId);
        item.softDelete();
        libraryItemRepository.save(item);
    }

    @Override
    @Transactional
    public void addExample(Long userId, Long itemId, LibraryRequestDTO.CreateExampleRequest req) {
        LibraryItem item = libraryValidator.validateOwnedItem(userId, itemId);

        LibraryItemExample ex = LibraryItemExample.of(item, req.exampleEn(), req.exampleKo(), req.source());
        libraryItemExampleRepository.save(ex);
    }

    @Override
    @Transactional
    public void deleteExample(Long userId, Long itemId, Long exampleId) {
        libraryValidator.validateOwnedItem(userId, itemId);
        libraryValidator.validateExampleOwned(itemId, exampleId);

        libraryItemExampleRepository.deleteById(exampleId);
    }

    // dto 매퍼들
    private LibraryResponseDTO.ItemSummary toSummary(LibraryItem li) {
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

    private LibraryResponseDTO.Source toSource(LibraryItemSource s) {
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

    private LibraryResponseDTO.Example toExample(LibraryItemExample e) {
        return new LibraryResponseDTO.Example(
                e.getId(),
                e.getExampleEn(),
                e.getExampleKo(),
                e.getSource().name(),
                e.getCreatedAt()
        );
    }

    // 단어를 정규화시킴(공백이랑 대소문자 통일)
    private String normalize(String phrase) {
        // lower + trim + collapse spaces
        return phrase == null ? "" : phrase.trim().toLowerCase().replaceAll("\\s+", " ");
    }
}
