package verbly.spring.domain.review.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.validator.constraints.Range;
import org.springframework.data.annotation.CreatedDate;
import verbly.spring.domain.review.dto.ReviewRequestDTO;
import verbly.spring.domain.user.entity.User;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder(access = AccessLevel.PRIVATE)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reviewee_id")
    private User reviewee;

    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @Range(min = 1, max = 5)
    private Integer rating;

    @Column(name = "review_content")
    private String reviewContent;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public static Review of(User reviewer, User reviewee, ReviewRequestDTO reviewRequestDTO) {

        return Review.builder()
                .reviewer(reviewer)
                .reviewee(reviewee)
                .rating(reviewRequestDTO.getRating())
                .reviewContent(reviewRequestDTO.getReviewContent())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
