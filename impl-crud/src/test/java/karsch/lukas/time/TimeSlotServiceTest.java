package karsch.lukas.time;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;

class TimeSlotServiceTest {
    private final DateTimeProvider dateTimeProvider = DateTimeProvider.getInstance();

    @BeforeEach
    void setUp() {
        dateTimeProvider.setClock(Clock.fixed(
                LocalDateTime.of(
                        2025,
                        1,
                        1,
                        1,
                        0
                ).toInstant(ZoneOffset.UTC),
                ZoneId.of("UTC")
        ));
    }

    @Test
    void testEarlier() {
        var earlierTimeSlot = new TimeSlotValueObject(
                LocalDate.of(2024, 1, 1),
                LocalTime.of(0, 0),
                LocalTime.of(1, 0)
        );

        var underTest = new TimeSlotService(dateTimeProvider);

        assertThat(underTest.isLive(earlierTimeSlot)).isFalse();
        assertThat(underTest.hasEnded(earlierTimeSlot)).isTrue();
    }

    @Test
    void testLive() {
        var liveTimeSlot = new TimeSlotValueObject(
                LocalDate.of(2025, 1, 1),
                LocalTime.of(0, 0),
                LocalTime.of(2, 0)
        );

        var underTest = new TimeSlotService(dateTimeProvider);

        assertThat(underTest.isLive(liveTimeSlot)).isTrue();
        assertThat(underTest.hasEnded(liveTimeSlot)).isFalse();
    }

    @Test
    void testLater() {
        var liveTimeSlot = new TimeSlotValueObject(
                LocalDate.of(2025, 2, 1),
                LocalTime.of(0, 0),
                LocalTime.of(1, 0)
        );

        var underTest = new TimeSlotService(dateTimeProvider);

        assertThat(underTest.isLive(liveTimeSlot)).isFalse();
        assertThat(underTest.hasEnded(liveTimeSlot)).isFalse();
    }
}
