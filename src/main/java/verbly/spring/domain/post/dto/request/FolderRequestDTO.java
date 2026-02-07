package verbly.spring.domain.post.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

public class FolderRequestDTO {
    @Getter
    @NoArgsConstructor
    public static class Create {
        private String name;
    }

    @Getter
    @NoArgsConstructor
    public static class Rename {
        private String name;
    }
}
