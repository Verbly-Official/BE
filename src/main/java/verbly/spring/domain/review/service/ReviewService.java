package verbly.spring.domain.review.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import verbly.spring.domain.review.dto.ReviewMetaResponseDTO;
import verbly.spring.domain.review.dto.ReviewRequestDTO;
import verbly.spring.domain.review.dto.ReviewResponseDTO;
import verbly.spring.domain.review.entity.Review;
import verbly.spring.domain.review.repo.ReviewRepository;
import verbly.spring.domain.user.entity.Stats;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.exception.UserHandler;
import verbly.spring.domain.user.repository.StatsRepository;
import verbly.spring.domain.user.repository.UserRepository;
import verbly.spring.global.common.code.ErrorStatus;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final StatsRepository statsRepository;

    // review only once? or not?
    public ReviewResponseDTO createReview(Long reviewerId, Long revieweeId, ReviewRequestDTO reviewRequestDTO) {

        //reviewer
        Optional<User> optionalReviewer = userRepository.findById(reviewerId);
        if (optionalReviewer.isEmpty()) throw new UserHandler(ErrorStatus.USER_NOT_FOUND);
        User reviewer = optionalReviewer.get();

        //reviewee
        Optional<User> optionalReviewee = userRepository.findById(revieweeId);
        if (optionalReviewee.isEmpty()) throw new UserHandler(ErrorStatus.USER_NOT_FOUND);
        User reviewee = optionalReviewee.get();

        //save
        Review review = Review.of(reviewer, reviewee, reviewRequestDTO);
        reviewRepository.save(review);

        //stats
        Optional<Stats> optionalStats = statsRepository.findById(revieweeId);
        if (optionalStats.isEmpty()) throw new UserHandler(ErrorStatus.USER_STATS_NOT_FOUND);
        Stats stats = optionalStats.get();

        //update stats_review
        stats.updateReviewMeta(stats.getReviewCount(), statsRepository.getAverageByRevieweeId(revieweeId));
        statsRepository.save(stats);

        return ReviewResponseDTO.from(review);
    }

    public List<ReviewResponseDTO> getReviewList(Long revieweeId) {

        Optional<User> optionalReviewee =  userRepository.findById(revieweeId);
        if (optionalReviewee.isEmpty()) throw new UserHandler(ErrorStatus.USER_NOT_FOUND);
        User reviewee = optionalReviewee.get();

        List<Review> reviewList = reviewRepository.findByReviewee(reviewee);

        // dto convert
        return reviewList
                .stream()
                .map(ReviewResponseDTO::from)
                .toList();
    }

    public ReviewMetaResponseDTO getReviewMeta(Long revieweeId) {

        Optional<Stats> optionalStats = statsRepository.findById(revieweeId);
        if (optionalStats.isEmpty()) throw new UserHandler(ErrorStatus.USER_STATS_NOT_FOUND);
        Stats stats = optionalStats.get();

        return ReviewMetaResponseDTO.from(stats, statsRepository.getRankPercentage(revieweeId));
    }
}
