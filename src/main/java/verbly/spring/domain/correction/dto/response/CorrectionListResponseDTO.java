package verbly.spring.domain.correction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CorrectionListResponseDTO {
    private long totalRequest;
    private List<CorrectionResponseDTO.MyCorrectionListDto> corrections;
}
