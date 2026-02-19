package verbly.spring.domain.library.controller;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import verbly.spring.domain.library.repository.LibraryItemSourceRepository;
import verbly.spring.domain.library.service.LibraryAutoFillService;
import verbly.spring.domain.library.service.LibraryAutoFillService.ExampleInput;
import verbly.spring.global.common.response.ApiResponse;
import verbly.spring.global.security.utils.SecurityUtils;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/library/autofill")
public class LibraryAutoFillController {

    private final LibraryAutoFillService libraryAutoFillService;
    private final LibraryItemSourceRepository libraryItemSourceRepository;

    @PostMapping("/corrections/{correctionId}")
    public ResponseEntity<ApiResponse<FillAndItemsResponse>> run(
            @PathVariable Long correctionId,
            @RequestBody(required = false) FillRequest request
    ) {
        Long userId = SecurityUtils.getCurrentUserId();

        List<ExampleInput> inputs = (request == null || request.getLibrary() == null)
                ? List.of()
                : request.getLibrary().stream()
                .map(card -> new ExampleInput(
                        card.getEn(),
                        card.getKo(),
                        card.getExamples() == null ? List.of()
                                : card.getExamples().stream()
                                .map(p -> new LibraryAutoFillService.ExamplePairInput(p.getExEn(), p.getExKo()))
                                .toList()
                ))
                .toList();

        var result = libraryAutoFillService.fillFromCorrection(userId, correctionId, inputs);

        List<Long> itemIds = libraryItemSourceRepository.findDistinctItemIdsByCorrectionId(correctionId);

        return ResponseEntity.ok(ApiResponse.onSuccess(new FillAndItemsResponse(result, itemIds)));
    }

    // ----- DTO -----

    @Getter
    @NoArgsConstructor
    public static class FillRequest {
        private List<LibraryCard> library;

        @Getter
        @NoArgsConstructor
        public static class LibraryCard {
            private String en;
            private String ko;
            private List<ExamplePair> examples;

            @Getter
            @NoArgsConstructor
            public static class ExamplePair {
                private String exEn;
                private String exKo;
            }
        }
    }

    public record FillAndItemsResponse(LibraryAutoFillService.FillResult fillResult, List<Long> itemIds) {}
}
