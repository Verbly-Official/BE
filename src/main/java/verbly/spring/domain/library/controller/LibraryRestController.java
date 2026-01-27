package verbly.spring.domain.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Library", description = "Library 탭 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/library")
public class LibraryRestController {

    private final LibraryQueryService libraryQueryService;
    private final LibraryCommandService libraryCommandService;

    /**
     * 아이템 생성 (현재는 테스트/임시 성격)
     */
    @Operation(
            summary = "라이브러리 아이템 생성",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = """
                    사용자의 라이브러리 아이템을 생성합니다.

                    ⚠️ 참고:
                    - 현재는 테스트/임시로 존재하는 API이며,
                      추후 첨삭(교정) 시점에 자동으로 DB에 들어가는 구조로 바뀌면 제거될 수 있습니다.

                    ✅ 요청 본문:
                    - phrase: 표현(필수)
                    - meaningKo: 의미(한국어, 선택)
                    - meaningEn: 의미(영어, 선택)
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "아이템 생성 요청 예시",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "CreateItemRequest 예시",
                            value = """
                                    {
                                      "phrase": "break the ice",
                                      "meaningKo": "어색한 분위기를 깨다",
                                      "meaningEn": "to relieve tension"
                                    }
                                    """
                    )
            )
    )
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<LibraryResponseDTO.CreateItemResponse>> createItem(
            @Valid @RequestBody LibraryRequestDTO.CreateItemRequest req
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        LibraryResponseDTO.CreateItemResponse res = libraryCommandService.createItem(userId, req);

        return ResponseEntity.status(SuccessStatus.LIBRARY_ITEM_CREATE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.LIBRARY_ITEM_CREATE_SUCCESS, res));
    }

    /**
     * 라이브러리 목록 조회
     */
    @Operation(
            summary = "라이브러리 아이템 목록 조회",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = """
                    라이브러리 아이템 목록을 조회합니다.

                    ✅ QueryString (모두 선택):
                    - q: 검색어(phrase 기준)
                    - starred: 즐겨찾기 필터
                    - page: 페이지(기본 0)
                    - size: 페이지 크기(기본 12)

                    ✅ 정렬:
                    - updatedAt DESC, id DESC
                    """
    )
    @Parameters({
            @Parameter(name = "q", description = "검색어 (phrase 기준)", example = "ice"),
            @Parameter(name = "starred", description = "즐겨찾기 필터", example = "true"),
            @Parameter(name = "page", description = "페이지 번호(0부터)", example = "0"),
            @Parameter(name = "size", description = "페이지 크기", example = "12")
    })
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

    /**
     * 라이브러리 상세 조회
     */
    @Operation(
            summary = "라이브러리 아이템 상세 조회",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = """
                    특정 라이브러리 아이템의 상세 정보를 조회합니다.
                    - sources(출처)와 examples(예문)까지 함께 반환합니다.
                    """
    )
    @Parameter(name = "itemId", description = "라이브러리 아이템 ID", example = "1", required = true)
    @GetMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<LibraryResponseDTO.ItemDetail>> detail(
            @PathVariable Long itemId
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        LibraryResponseDTO.ItemDetail body = libraryQueryService.getItemDetail(userId, itemId);

        return ResponseEntity.status(SuccessStatus.LIBRARY_ITEM_DETAIL_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.LIBRARY_ITEM_DETAIL_SUCCESS, body));
    }

    /**
     * 아이템 업데이트(현재는 starred만)
     */
    @Operation(
            summary = "라이브러리 아이템 업데이트",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = """
                    라이브러리 아이템을 수정합니다.

                    ✅ 현재 수정 가능한 값:
                    - starred: 즐겨찾기 여부(Boolean, 선택)
                    """
    )
    @Parameter(name = "itemId", description = "라이브러리 아이템 ID", example = "1", required = true)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "아이템 수정 요청 예시(starred)",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "UpdateItemRequest 예시",
                            value = """
                                    {
                                      "starred": true
                                    }
                                    """
                    )
            )
    )
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

    /**
     * 아이템 삭제(soft delete)
     */
    @Operation(
            summary = "라이브러리 아이템 삭제",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = "라이브러리 아이템을 삭제합니다. (soft delete 정책)"
    )
    @Parameter(name = "itemId", description = "라이브러리 아이템 ID", example = "1", required = true)
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long itemId) {
        Long userId = SecurityUtils.getCurrentUserId();
        libraryCommandService.deleteItem(userId, itemId);

        return ResponseEntity.status(SuccessStatus.LIBRARY_ITEM_DELETE_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.LIBRARY_ITEM_DELETE_SUCCESS, null));
    }

    /**
     * 예문 추가
     */
    @Operation(
            summary = "라이브러리 아이템 예문 추가",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = """
                    특정 아이템에 예문을 추가합니다.

                    ✅ 요청 본문:
                    - exampleEn: 영어 예문(필수)
                    - exampleKo: 한국어 해석(선택)
                    - source: 예문 출처(AI / MANUAL / CORPUS)
                    """
    )
    @Parameter(name = "itemId", description = "라이브러리 아이템 ID", example = "1", required = true)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "예문 추가 요청 예시",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "CreateExampleRequest 예시",
                            value = """
                                    {
                                      "exampleEn": "I tried to break the ice with a joke.",
                                      "exampleKo": "나는 농담으로 어색한 분위기를 깨려고 했다.",
                                      "source": "MANUAL"
                                    }
                                    """
                    )
            )
    )
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

    /**
     * 예문 삭제
     */
    @Operation(
            summary = "라이브러리 아이템 예문 삭제",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = "특정 아이템의 예문을 삭제합니다."
    )
    @Parameters({
            @Parameter(name = "itemId", description = "라이브러리 아이템 ID", example = "1", required = true),
            @Parameter(name = "exampleId", description = "예문 ID", example = "10", required = true)
    })
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
