package verbly.spring.domain.library.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import verbly.spring.domain.library.dto.request.LibraryRequestDTO;
import verbly.spring.domain.library.dto.response.LibraryResponseDTO;
import verbly.spring.domain.library.service.LibraryService;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.common.exception.BaseException;
import verbly.spring.global.common.response.ApiResponse;
import verbly.spring.global.security.auth.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/library")
public class LibraryRestController {

    private final LibraryService libraryService;

    /** 시큐리티에서 현재 로그인 된 유저 아이디 빼오기*/
    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            throw new BaseException(ErrorStatus._UNAUTHORIZED);
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof CustomUserDetails cud) {
            return cud.getUserId();
        }

        throw new BaseException(ErrorStatus._UNAUTHORIZED);
    }

    /** (나중에는 변경되어야하는데 테스트를 해야하니까 임시로 api추가함 ) 라이브러리 아이템 생성 */
    @PostMapping("/items")
    public ApiResponse<LibraryResponseDTO.CreateItemResponse> createItem(
            @Valid @RequestBody LibraryRequestDTO.CreateItemRequest req
    ) {
        Long userId = currentUserId();
        return ApiResponse.onSuccess(libraryService.createItem(userId, req));
    }

    /** 라이브러리 목록(서치랑 필터 북마크 쿼리로 넣어줘야함) */
    @GetMapping("/items")
    public ApiResponse<LibraryResponseDTO.PageResult<LibraryResponseDTO.ItemSummary>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean starred,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Long userId = currentUserId();

        //  보이는 순서는 업데이트 순!
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt", "id"));
        Page<LibraryResponseDTO.ItemSummary> result = libraryService.listItems(userId, q, starred, pageable);

        LibraryResponseDTO.PageResult<LibraryResponseDTO.ItemSummary> body =
                new LibraryResponseDTO.PageResult<>(
                        result.getContent(),
                        result.getNumber(),
                        result.getSize(),
                        result.getTotalElements(),
                        result.getTotalPages(),
                        result.isLast()
                );

        return ApiResponse.onSuccess(body);
    }

    /** 라이브러리 상세 */
    @GetMapping("/items/{itemId}")
    public ApiResponse<LibraryResponseDTO.ItemDetail> detail(@PathVariable Long itemId) {
        Long userId = currentUserId();
        return ApiResponse.onSuccess(libraryService.getItemDetail(userId, itemId));
    }

    /** 아이템 업데이트(북마크 달기) */
    @PatchMapping("/items/{itemId}")
    public ApiResponse<Void> update(
            @PathVariable Long itemId,
            @RequestBody LibraryRequestDTO.UpdateItemRequest req
    ) {
        Long userId = currentUserId();
        libraryService.updateItem(userId, itemId, req);
        return ApiResponse.onSuccess(null);
    }

    /** 아이템 삭제(soft delete) */
    @DeleteMapping("/items/{itemId}")
    public ApiResponse<Void> delete(@PathVariable Long itemId) {
        Long userId = currentUserId();
        libraryService.deleteItem(userId, itemId);
        return ApiResponse.onSuccess(null);
    }

    /** 예문 추가 */
    @PostMapping("/items/{itemId}/examples")
    public ApiResponse<Void> addExample(
            @PathVariable Long itemId,
            @Valid @RequestBody LibraryRequestDTO.CreateExampleRequest req
    ) {
        Long userId = currentUserId();
        libraryService.addExample(userId, itemId, req);
        return ApiResponse.onSuccess(null);
    }

    /** 예문 삭제(얘도 soft delete임) */
    @DeleteMapping("/items/{itemId}/examples/{exampleId}")
    public ApiResponse<Void> deleteExample(
            @PathVariable Long itemId,
            @PathVariable Long exampleId
    ) {
        Long userId = currentUserId();
        libraryService.deleteExample(userId, itemId, exampleId);
        return ApiResponse.onSuccess(null);
    }
}
