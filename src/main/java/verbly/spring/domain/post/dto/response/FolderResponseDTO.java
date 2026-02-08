package verbly.spring.domain.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import verbly.spring.domain.post.entity.Folder;

import java.time.LocalDateTime;
import java.util.List;

public class FolderResponseDTO {
    @Getter
    @Builder
    public static class FolderInfo {
        private Long id;
        private String name;
        private LocalDateTime createdAt;

        public static FolderInfo from(Folder folder) {
            return FolderInfo.builder()
                    .id(folder.getId())
                    .name(folder.getName())
                    .createdAt(folder.getCreatedAt())
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CreateResult {
        private Long folderId;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class FolderListResult {
        private int count;
        private List<FolderInfo> folders;

        public static FolderListResult of(List<FolderInfo> folders) {
            return FolderListResult.builder()
                    .count(folders.size())
                    .folders(folders)
                    .build();
        }
    }
}
