package verbly.spring.domain.library.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LibraryAiRequest {

    private String original;

    private String revised;
}