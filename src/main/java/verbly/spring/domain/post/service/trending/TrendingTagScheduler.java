package verbly.spring.domain.post.service.trending;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.post.entity.Tag;
import verbly.spring.domain.post.entity.TrendingTag;
import verbly.spring.domain.post.repository.PostTagRepository;
import verbly.spring.domain.post.repository.TrendingTagRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TrendingTagScheduler {

    private final PostTagRepository postTagRepository;
    private final TrendingTagRepository trendingTagRepository;



    //테스트용
    //@Scheduled(cron = "0 * * * * *")

    @Scheduled(cron = "0 0 0 * * *")
    public void updateTrendingTags() {

        LocalDate yesterday = LocalDate.now().minusDays(1);

        //테스트용
        //LocalDate yesterday = LocalDate.now();

        LocalDateTime startOfDay = yesterday.atStartOfDay();
        LocalDateTime endOfDay = yesterday.atTime(LocalTime.MAX);

        Pageable pageable = PageRequest.of(0, 10);
        List<Tag> results = postTagRepository.findTrendingTagIds(startOfDay, endOfDay, pageable);
        log.info("조회된 태그 개수: {}", results.size());
        trendingTagRepository.deleteAllInBatch();

        int rank = 1;
        for (Tag tag : results) {
            TrendingTag trendingTag = TrendingTag.builder()
                    .ranking(rank++)
                    .tag(tag)
                    .build();
            trendingTagRepository.save(trendingTag);
        }
    }
}
