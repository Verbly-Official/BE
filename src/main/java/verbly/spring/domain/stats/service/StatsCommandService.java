package verbly.spring.domain.stats.service;

import verbly.spring.domain.user.entity.User;

public interface StatsCommandService {
    void markAttendance(Long userId, String timezone);
    void gainPoint(Long userId, long amount);
    void markAttendanceIfNeeded(User user);
}
