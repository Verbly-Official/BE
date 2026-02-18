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

        // ✅ library 카드(en/ko/exEn/exKo)로 받기
        List<ExampleInput> inputs = (request == null || request.getLibrary() == null)
                ? List.of()
                : request.getLibrary().stream()
                .map(e -> new ExampleInput(e.getEn(), e.getKo(), e.getExEn(), e.getExKo()))
                .toList();

        var result = libraryAutoFillService.fillFromCorrection(userId, correctionId, inputs);

        // source 기준으로 itemId 뽑기
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
            private String en;    // 원형(lemma)
            private String ko;    // 뜻(한국어)
            private String exEn;  // 원형 포함 예문(EN)
            private String exKo;  // 예문 뜻(KO)
        }
    }

    public record FillAndItemsResponse(LibraryAutoFillService.FillResult fillResult, List<Long> itemIds) {}
}
