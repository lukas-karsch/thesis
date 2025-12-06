package karsch.lukas.core.time;

import lombok.Getter;
import lombok.Setter;

import java.time.Clock;

/**
 * Allows controlling the application time.
 * adapted from <a href="https://sahan-r.medium.com/time-travel-in-spring-boot-6bba96d38e03">Sahan Ranasinghe</a>
 */
public class DateTimeProvider {

    private static DateTimeProvider instance;

    private final Clock defaultClock = Clock.systemDefaultZone();

    @Getter
    @Setter
    private Clock clock = defaultClock;

    public static DateTimeProvider getInstance() {
        if (instance == null) {
            instance = new DateTimeProvider();
        }
        return instance;
    }

    public void resetTime() {
        this.clock = this.defaultClock;
    }
}
