package verbly.spring.domain.post.service.hotpost;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.post.entity.HotPost;
import verbly.spring.domain.post.entity.Post;
import verbly.spring.domain.post.entity.PostLikeHistory;
import verbly.spring.domain.post.enums.PostStatus;
import verbly.spring.domain.post.repository.HotPostRepository;
import verbly.spring.domain.post.repository.PostLikeHistoryRepository;
import verbly.spring.domain.post.repository.PostRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotPostScheduler {

    private final PostRepository postRepository;
    private final HotPostRepository hotPostRepository;
    private final PostLikeHistoryRepository historyRepository;

    @Scheduled(fixedRate = 1800000)
    @Transactional
    public void runBatch() {
        updateHotPosts();

        saveCurrentSnapshot();
    }

    private void updateHotPosts() {
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);

        List<Object[]> top10Results = postRepository.findTop10RisingPosts(10, twentyFourHoursAgo);

        List<HotPost> newHotPosts = top10Results.stream()
                .map(result -> {
                    Long postId = ((Number) result[0]).longValue();
                    int growth = ((Number) result[1]).intValue();
                    Post post = postRepository.findById(postId)
                            .orElseThrow(() -> new RuntimeException("Post not found: " + postId));
                    return HotPost.builder().post(post).growthScore(growth).build();
                })
                .toList();

        hotPostRepository.deleteAll();
        hotPostRepository.saveAll(newHotPosts);
    }

    private void saveCurrentSnapshot() {
        List<Post> candidates = postRepository.findByStatusAndLikesCountGreaterThanEqual(
                PostStatus.COMPLETED, 10
        );

        List<PostLikeHistory> histories = candidates.stream()
                .map(post -> PostLikeHistory.builder()
                        .postId(post.getId())
                        .likeCount(post.getLikesCount())
                        .recordedAt(LocalDateTime.now())
                        .build())
                .toList();
        historyRepository.saveAll(histories);
    }
}