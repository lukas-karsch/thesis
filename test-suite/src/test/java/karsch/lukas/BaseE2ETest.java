package karsch.lukas;

import java.time.Clock;

public interface BaseE2ETest {
    int getPort();

    void resetDatabase();

    void setSystemTime(Clock clock);
}
