package verbly.spring.domain.review.dto;

import lombok.*;
import verbly.spring.domain.review.entity.Review;
import verbly.spring.domain.user.entity.User;

import java.time.LocalDateTime;

@Builder(access = AccessLevel.PRIVATE)
@Getter
@AllArgsConstructor
public class ReviewResponseDTO {

    private String reviewerName;

    private String nativeLang;

    private String imageUrl;

    private Integer rating;

    private String reviewContent;

    private LocalDateTime createdAt;

    public static ReviewResponseDTO from(Review review) {

        User reviewer = review.getReviewer();

        return ReviewResponseDTO.builder()
                .reviewerName(reviewer.getNickname())
                .nativeLang(reviewer.getNativeLang())
                .imageUrl(reviewer.getProfileImage().getImageUrl())
                .rating(review.getRating())
                .reviewContent(review.getReviewContent())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
