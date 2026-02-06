package verbly.spring.domain.stats.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.stats.entity.Stats;
import verbly.spring.domain.stats.exception.StatsHandler;
import verbly.spring.domain.stats.repository.StatsRepository;
import verbly.spring.domain.user.entity.User;
import verbly.spring.global.common.code.ErrorStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsCommandServiceImpl implements StatsCommandService {
    private final StatsRepository statsRepository;

    @Override
    public void markAttendance(Long userId, String timezone) {
        Stats stats = statsRepository.findByUserId(userId)
                .orElseThrow(() -> new StatsHandler(ErrorStatus.STATS_NOT_FOUND));

        stats.markAttendance(timezone);
    }

    @Override
    public void gainPoint(Long userId, long amount) {
        Stats stats = statsRepository.findByUserId(userId)
                .orElseThrow(() -> new StatsHandler(ErrorStatus.STATS_NOT_FOUND));

        stats.setPoint(stats.getPoint() + amount);
    }
}
