package verbly.spring.global.common.utils;

import java.time.Duration;
import java.time.LocalDateTime;

public class RelativeTimeUtils {
    public static String toRelative(LocalDateTime time) {
        if (time == null) return null;

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(time, now);

        long days = duration.toDays();

        if (days == 0) {
            return "Today";
        }
        if (days == 1) {
            return "Yesterday";
        }
        return days + " days ago";
    }
}
