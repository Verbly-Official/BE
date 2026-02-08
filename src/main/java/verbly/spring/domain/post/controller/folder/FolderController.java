package verbly.spring.domain.post.controller.folder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import verbly.spring.domain.post.dto.request.FolderRequestDTO;
import verbly.spring.domain.post.dto.response.FolderResponseDTO;
import verbly.spring.domain.post.service.folder.FolderService;
import verbly.spring.global.security.utils.SecurityUtils;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/correction/folders")
public class FolderController {

    private final FolderService folderService;

    /**
     * 폴더 생성
     */
    @Operation(
            summary = "폴더 생성",
            description = "커렉션 폴더를 생성합니다.",
            security = { @SecurityRequirement(name = "JWT TOKEN") }
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "폴더 생성 요청",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "폴더 생성",
                                    value = """
                                    {
                                      "name": "비즈니스용"
                                    }
                                    """
                            )
                    }
            )
    )

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FolderResponseDTO.CreateResult createFolder(
            @RequestBody FolderRequestDTO.Create request
    ) {
        Long ownerId = SecurityUtils.getCurrentUserId();
        Long folderId = folderService.createFolder(ownerId, request);
        return FolderResponseDTO.CreateResult.builder()
                .folderId(folderId)
                .build();
    }

    @Operation(
            summary = "폴더 목록 조회",
            description = "최근 생성 순으로 커렉션 폴더 목록을 조회합니다.",
            security = { @SecurityRequirement(name = "JWT TOKEN") }
    )
    @GetMapping
    public FolderResponseDTO.FolderListResult getMyFolders() {
        Long ownerId = SecurityUtils.getCurrentUserId();
        List<FolderResponseDTO.FolderInfo> folders = folderService.getMyFolders(ownerId);
        return FolderResponseDTO.FolderListResult.of(folders);
    }
}
