package verbly.spring.domain.library.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import verbly.spring.domain.library.dto.request.LibraryRequestDTO;
import verbly.spring.domain.library.dto.response.LibraryResponseDTO;
import verbly.spring.domain.library.service.LibraryCommandService;
import verbly.spring.domain.library.service.LibraryQueryService;
import verbly.spring.global.common.code.SuccessStatus;
import verbly.spring.global.common.response.ApiResponse;
import verbly.spring.global.security.utils.SecurityUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/library")
public class LibraryRestController {

    private final LibraryQueryService libraryQueryService;
    private final LibraryCommandService libraryCommandService;

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<LibraryResponseDTO.CreateItemResponse>> createItem(
            @Valid @RequestBody LibraryRequestDTO.CreateItemRequest req
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        LibraryResponseDTO.CreateItemResponse res = libraryCommandService.createItem(userId, req);
        return ResponseEntity.status(SuccessStatus.LIBRARY_ITEM_CREATE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.LIBRARY_ITEM_CREATE_SUCCESS, res));
    }

    @GetMapping("/items")
    public ResponseEntity<ApiResponse<LibraryResponseDTO.PageResult<LibraryResponseDTO.ItemSummary>>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean starred,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Long userId = SecurityUtils.getCurrentUserId();

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt", "id"));
        LibraryResponseDTO.PageResult<LibraryResponseDTO.ItemSummary> body =
                libraryQueryService.listItems(userId, q, starred, pageable);

        return ResponseEntity.status(SuccessStatus.LIBRARY_ITEM_LIST_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.LIBRARY_ITEM_LIST_SUCCESS, body));
    }

    @GetMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<LibraryResponseDTO.ItemDetail>> detail(@PathVariable Long itemId) {
        Long userId = SecurityUtils.getCurrentUserId();
        LibraryResponseDTO.ItemDetail body = libraryQueryService.getItemDetail(userId, itemId);
        return ResponseEntity.status(SuccessStatus.LIBRARY_ITEM_DETAIL_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.LIBRARY_ITEM_DETAIL_SUCCESS, body));
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long itemId,
            @RequestBody LibraryRequestDTO.UpdateItemRequest req
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        libraryCommandService.updateItem(userId, itemId, req);
        return ResponseEntity.status(SuccessStatus.LIBRARY_ITEM_UPDATE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.LIBRARY_ITEM_UPDATE_SUCCESS, null));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long itemId) {
        Long userId = SecurityUtils.getCurrentUserId();
        libraryCommandService.deleteItem(userId, itemId);
        return ResponseEntity.status(SuccessStatus.LIBRARY_ITEM_DELETE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.LIBRARY_ITEM_DELETE_SUCCESS, null));
    }

    @PostMapping("/items/{itemId}/examples")
    public ResponseEntity<ApiResponse<Void>> addExample(
            @PathVariable Long itemId,
            @Valid @RequestBody LibraryRequestDTO.CreateExampleRequest req
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        libraryCommandService.addExample(userId, itemId, req);
        return ResponseEntity.status(SuccessStatus.LIBRARY_ITEM_EXAMPLE_ADD_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.LIBRARY_ITEM_EXAMPLE_ADD_SUCCESS, null));
    }

    @DeleteMapping("/items/{itemId}/examples/{exampleId}")
    public ResponseEntity<ApiResponse<Void>> deleteExample(
            @PathVariable Long itemId,
            @PathVariable Long exampleId
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        libraryCommandService.deleteExample(userId, itemId, exampleId);
        return ResponseEntity.status(SuccessStatus.LIBRARY_ITEM_EXAMPLE_DELETE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.LIBRARY_ITEM_EXAMPLE_DELETE_SUCCESS, null));
    }
}
