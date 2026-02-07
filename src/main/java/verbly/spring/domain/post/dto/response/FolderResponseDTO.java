package verbly.spring.domain.post.dto.response;

import lombok.Builder;
import lombok.Getter;
import verbly.spring.domain.post.entity.Folder;

public class FolderResponseDTO {
    @Getter
    @Builder
    public static class FolderInfo {
        private Long id;
        private String name;
        private Integer sortOrder;

        public static FolderInfo from(Folder folder) {
            return FolderInfo.builder()
                    .id(folder.getId())
                    .name(folder.getName())
                    .build();
        }
    }
}
