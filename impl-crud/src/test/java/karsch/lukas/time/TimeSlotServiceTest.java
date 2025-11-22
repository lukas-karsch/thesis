package karsch.lukas.time;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Set;
import java.util.TreeSet;

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

    /**
     * Identical slots
     * +-----+
     * +-----+
     */
    @Test
    void testOverlap_identicalTimeslots() {
        var day = LocalDate.of(2025, 11, 1);

        var slot1 = new TimeSlotValueObject(
                day,
                LocalTime.of(11, 0),
                LocalTime.of(12, 0)
        );

        var slot2 = new TimeSlotValueObject(
                day,
                LocalTime.of(11, 0),
                LocalTime.of(12, 0)
        );

        var underTest = new TimeSlotService(dateTimeProvider);

        assertThat(underTest.areOverlapping(slot1, slot2)).isTrue();
        assertThat(underTest.areOverlapping(slot2, slot1)).isTrue();
    }

    /**
     * No overlap
     * +-----+
     * +-----+
     */
    @Test
    void testOverlap_sameDay_notOverlappingTime() {
        var day = LocalDate.of(2025, 11, 1);

        var slot1 = new TimeSlotValueObject(
                day,
                LocalTime.of(11, 0),
                LocalTime.of(12, 0)
        );

        var slot2 = new TimeSlotValueObject(
                day,
                LocalTime.of(12, 15),
                LocalTime.of(13, 15)
        );

        var underTest = new TimeSlotService(dateTimeProvider);

        assertThat(underTest.areOverlapping(slot1, slot2)).isFalse();
        assertThat(underTest.areOverlapping(slot2, slot1)).isFalse();
    }

    /**
     * Overlap
     * +-----+
     * +-----+
     */
    @Test
    void testOverlap_startTime_beforeOtherEndTime() {
        var day = LocalDate.of(2025, 11, 1);

        var slot1 = new TimeSlotValueObject(
                day,
                LocalTime.of(11, 0),
                LocalTime.of(12, 0)
        );

        var slot2 = new TimeSlotValueObject(
                day,
                LocalTime.of(11, 30),
                LocalTime.of(13, 0)
        );

        var underTest = new TimeSlotService(dateTimeProvider);

        assertThat(underTest.areOverlapping(slot1, slot2)).isTrue();
        assertThat(underTest.areOverlapping(slot2, slot1)).isTrue();
    }

    /**
     * One inside the other
     * +---------+
     * +----+
     */
    @Test
    void testOverlap_oneInsideTheOther() {
        var day = LocalDate.of(2025, 11, 1);

        var slot1 = new TimeSlotValueObject(
                day,
                LocalTime.of(10, 0),
                LocalTime.of(13, 0)
        );

        var slot2 = new TimeSlotValueObject(
                day,
                LocalTime.of(11, 0),
                LocalTime.of(12, 0)
        );

        var underTest = new TimeSlotService(dateTimeProvider);

        assertThat(underTest.areOverlapping(slot1, slot2)).isTrue();
        assertThat(underTest.areOverlapping(slot2, slot1)).isTrue();
    }

    @Test
    void testAreConflictingTimeSlots_withNoConflict() {
        var slots1 = new TreeSet<>(new TimeSlotComparator());
        slots1.addAll(Set.of(
                new TimeSlotValueObject(
                        LocalDate.of(2025, 11, 1),
                        LocalTime.of(10, 0),
                        LocalTime.of(12, 0)
                ), new TimeSlotValueObject(
                        LocalDate.of(2025, 11, 8),
                        LocalTime.of(10, 0),
                        LocalTime.of(12, 0)
                ), new TimeSlotValueObject(
                        LocalDate.of(2025, 11, 15),
                        LocalTime.of(10, 0),
                        LocalTime.of(12, 0)
                )
        ));

        var slots2 = new TreeSet<>(new TimeSlotComparator());
        slots2.addAll(Set.of(
                new TimeSlotValueObject(
                        LocalDate.of(2025, 11, 1),
                        LocalTime.of(12, 15),
                        LocalTime.of(14, 0)
                ), new TimeSlotValueObject(
                        LocalDate.of(2025, 11, 8),
                        LocalTime.of(12, 15),
                        LocalTime.of(14, 0)
                ), new TimeSlotValueObject(
                        LocalDate.of(2025, 11, 15),
                        LocalTime.of(12, 15),
                        LocalTime.of(14, 0)
                )
        ));

        var underTest = new TimeSlotService(dateTimeProvider);

        assertThat(underTest.areConflictingTimeSlots(slots1, slots2)).isFalse();
    }

    @Test
    void testAreConflictingTimeSlots_withConflict() {
        var slots1 = new TreeSet<>(new TimeSlotComparator());
        slots1.addAll(Set.of(
                new TimeSlotValueObject(
                        LocalDate.of(2025, 11, 1),
                        LocalTime.of(10, 0),
                        LocalTime.of(12, 0)
                ), new TimeSlotValueObject(
                        LocalDate.of(2025, 11, 8),
                        LocalTime.of(10, 0),
                        LocalTime.of(12, 0)
                ), new TimeSlotValueObject(
                        LocalDate.of(2025, 11, 15),
                        LocalTime.of(10, 0),
                        LocalTime.of(12, 0)
                )
        ));

        var slots2 = new TreeSet<>(new TimeSlotComparator());
        slots2.addAll(Set.of(
                new TimeSlotValueObject(
                        LocalDate.of(2025, 11, 1),
                        LocalTime.of(12, 15),
                        LocalTime.of(14, 0)
                ), new TimeSlotValueObject(
                        LocalDate.of(2025, 11, 8),
                        LocalTime.of(12, 15),
                        LocalTime.of(14, 0)
                ), new TimeSlotValueObject(
                        LocalDate.of(2025, 11, 15),
                        LocalTime.of(10, 0), // overlap here!
                        LocalTime.of(14, 0)
                )
        ));

        var underTest = new TimeSlotService(dateTimeProvider);

        assertThat(underTest.areConflictingTimeSlots(slots1, slots2)).isTrue();
    }
}
