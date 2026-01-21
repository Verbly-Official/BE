package verbly.spring.domain.stats.exception;

public class StatsHandler extends RuntimeException {
    public StatsHandler(String message) {
        super(message);
    }
}
