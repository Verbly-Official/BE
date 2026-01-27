package verbly.spring.domain.correction.converter;

import verbly.spring.domain.correction.dto.response.CorrectionResponseDTO;
import verbly.spring.domain.correction.entity.Correction;
import verbly.spring.domain.correction.enums.CorrectorType;
import verbly.spring.domain.post.entity.Post;


public class CorrectionConverter {
    private CorrectionConverter() {}

    public static CorrectionResponseDTO.CreateCorrectionResponseDTO toCreateCorrectionResponse(Correction correction) {
        return CorrectionResponseDTO.CreateCorrectionResponseDTO.builder()
                .correctionId(correction.getId())
                .postId(correction.getPost().getId())
                .build();
    }

    public static CorrectionResponseDTO.MyCorrectionDto toMyCorrectionDTO(
            Correction correction,
            CorrectorType latestCorrectorType,
            String latestCorrectorName
    ) {
        Post post = correction.getPost();

        return CorrectionResponseDTO.MyCorrectionDto.builder()
                .correctionId(correction.getId())
                .postId(post.getId())
                .title(post.getTitle())
                .correctorType(latestCorrectorType)
                .correctorName(latestCorrectorName)
                .correctionCreatedAt(correction.getCreatedAt())
                .status(post.getStatus())
                .build();
    }
}
