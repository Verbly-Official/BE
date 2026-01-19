package verbly.spring.domain.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import verbly.spring.domain.library.enums.ExampleSource;

public class LibraryRequestDTO {

    /** 라이브러리 아이템 직접 생성(필요하면 사용) */
    public record CreateItemRequest(
            @NotBlank @Size(max = 255) String phrase,
            String meaningKo,
            String meaningEn
    ) {}

    /** 아이템 업데이트: 현재는 starred만 (meaning 편집은 정책에 따라 추가 가능) */
    public record UpdateItemRequest(
            Boolean starred
    ) {}

    /** 예문 추가 */
    public record CreateExampleRequest(
            @NotBlank String exampleEn,
            String exampleKo,
            ExampleSource source
    ) {}
}
