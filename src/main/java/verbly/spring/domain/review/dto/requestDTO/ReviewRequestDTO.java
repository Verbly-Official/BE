package verbly.spring.domain.review.dto.requestDTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

@NoArgsConstructor
@Getter
public class ReviewRequestDTO {

    @NotNull
    @Range(min = 1, max = 5)
    private Integer rating;

    @NotEmpty(message = "본문을 입력해주세요")
    private String reviewContent;
}
