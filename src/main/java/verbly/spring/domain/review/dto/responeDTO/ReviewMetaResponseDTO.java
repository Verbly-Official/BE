package verbly.spring.domain.review.dto.responeDTO;

import lombok.*;
import verbly.spring.domain.stats.entity.Stats;

@Builder(access = AccessLevel.PRIVATE)
@Getter
@AllArgsConstructor
public class ReviewMetaResponseDTO {

    private Double reviewAverage;

    private Long reviewCount;

    private Double rankPercentage;

    public static ReviewMetaResponseDTO from(Stats stats, Double rankPercentage) {

        return ReviewMetaResponseDTO.builder()
                .reviewAverage(stats.getReviewAverage())
                .reviewCount(stats.getReviewCount())
                .rankPercentage(rankPercentage)
                .build();
    }
}
