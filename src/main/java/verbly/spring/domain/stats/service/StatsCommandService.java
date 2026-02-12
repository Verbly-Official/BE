package verbly.spring.domain.stats.service;

public interface StatsCommandService {
    void checkAttendance(Long userId, String timezone);
    void gainPoint(Long userId, long amount);
}
