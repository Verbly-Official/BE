package verbly.spring.domain.post.service.folder;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.post.dto.request.FolderRequestDTO;
import verbly.spring.domain.post.dto.response.FolderResponseDTO;
import verbly.spring.domain.post.entity.Folder;
import verbly.spring.domain.post.exception.PostHandler;
import verbly.spring.domain.post.repository.FolderRepository;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.repository.UserRepository;
import verbly.spring.global.common.code.ErrorStatus;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FolderService {
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long createFolder(Long ownerId, FolderRequestDTO.Create request) {
        String name = extractNameOrThrow(request);

        if (folderRepository.existsByOwnerIdAndName(ownerId, name)) {
            throw new PostHandler(ErrorStatus.FOLDER_NAME_DUPLICATE);
        }

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new PostHandler(ErrorStatus.USER_NOT_FOUND));

        Folder folder = Folder.builder()
                .owner(owner)
                .name(name)
                .build();

        return folderRepository.save(folder).getId();
    }

    public List<FolderResponseDTO.FolderInfo> getMyFolders(Long ownerId) {
        return folderRepository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId)
                .stream()
                .map(FolderResponseDTO.FolderInfo::from)
                .toList();
    }

    @Transactional
    public void renameFolder(Long ownerId, Long folderId, FolderRequestDTO.Rename request) {
        String newName = extractNameOrThrow(request);

        Folder folder = folderRepository.findByIdAndOwnerId(folderId, ownerId)
                .orElseThrow(() -> new PostHandler(ErrorStatus.FOLDER_NOT_FOUND));

        if (!folder.getName().equals(newName)
                && folderRepository.existsByOwnerIdAndName(ownerId, newName)) {
            throw new PostHandler(ErrorStatus.FOLDER_NAME_DUPLICATE);
        }

        folder.rename(newName);
    }

    @Transactional
    public void deleteFolder(Long ownerId, Long folderId) {
        Folder folder = folderRepository.findByIdAndOwnerId(folderId, ownerId)
                .orElseThrow(() -> new PostHandler(ErrorStatus.FOLDER_NOT_FOUND));

        folderRepository.delete(folder);
    }


    private String extractNameOrThrow(FolderRequestDTO.Create request) {
        if (request == null) throw new PostHandler(ErrorStatus._BAD_REQUEST);
        return normalizeAndValidateName(request.getName());
    }

    private String extractNameOrThrow(FolderRequestDTO.Rename request) {
        if (request == null) throw new PostHandler(ErrorStatus._BAD_REQUEST);
        return normalizeAndValidateName(request.getName());
    }

    private String normalizeAndValidateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new PostHandler(ErrorStatus.FOLDER_NAME_NOT_EXIST);
        }
        String trimmed = name.trim();
        if (trimmed.length() > 50) {
            throw new PostHandler(ErrorStatus.FOLDER_NAME_TOO_LONG);
        }
        return trimmed;
    }
}
