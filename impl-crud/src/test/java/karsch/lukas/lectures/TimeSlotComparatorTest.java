package karsch.lukas.lectures;

import karsch.lukas.time.TimeSlotComparator;
import karsch.lukas.time.TimeSlotValueObject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeSlotComparatorTest {

    private final TimeSlotComparator comparator = new TimeSlotComparator();

    private final LocalDate DATE = LocalDate.of(2024, 1, 1);
    private final LocalTime START = LocalTime.of(10, 0);
    private final LocalTime END = LocalTime.of(12, 0);

    @Test
    void shouldReturnNegative_WhenDateIsEarlier() {
        var slotA = new TimeSlotValueObject(DATE.minusDays(1), START, END);
        var slotB = new TimeSlotValueObject(DATE, START, END);

        int result = comparator.compare(slotA, slotB);

        assertTrue(result < 0, "Should return negative when first date is earlier");
    }

    @Test
    void shouldReturnPositive_WhenDateIsLater() {
        var slotA = new TimeSlotValueObject(DATE.plusDays(1), START, END);
        var slotB = new TimeSlotValueObject(DATE, START, END);

        int result = comparator.compare(slotA, slotB);

        assertTrue(result > 0, "Should return positive when first date is later");
    }

    @Test
    void shouldPrioritizeDate_OverTime() {
        var slotA = new TimeSlotValueObject(DATE.minusDays(1), START.plusHours(5), END);
        var slotB = new TimeSlotValueObject(DATE, START, END);

        int result = comparator.compare(slotA, slotB);

        assertTrue(result < 0, "Should return negative based on Date, even if StartTime is later");
    }

    @Test
    void shouldReturnNegative_WhenDateEqual_AndStartTimeEarlier() {
        var slotA = new TimeSlotValueObject(DATE, START.minusHours(1), END);
        var slotB = new TimeSlotValueObject(DATE, START, END);

        int result = comparator.compare(slotA, slotB);

        assertTrue(result < 0, "Should return negative when Date matches but StartTime is earlier");
    }

    @Test
    void shouldReturnPositive_WhenDateEqual_AndStartTimeLater() {
        var slotA = new TimeSlotValueObject(DATE, START.plusHours(1), END);
        var slotB = new TimeSlotValueObject(DATE, START, END);

        int result = comparator.compare(slotA, slotB);

        assertTrue(result > 0, "Should return positive when Date matches but StartTime is later");
    }

    @Test
    void shouldPrioritizeStartTime_OverEndTime() {
        var slotA = new TimeSlotValueObject(DATE, START.minusHours(1), END.plusHours(5));
        var slotB = new TimeSlotValueObject(DATE, START, END);

        int result = comparator.compare(slotA, slotB);

        assertTrue(result < 0, "Should return negative based on StartTime, even if EndTime is later");
    }

    @Test
    void shouldReturnNegative_WhenDateAndStartEqual_AndEndTimeEarlier() {
        var slotA = new TimeSlotValueObject(DATE, START, END.minusHours(1));
        var slotB = new TimeSlotValueObject(DATE, START, END);

        int result = comparator.compare(slotA, slotB);

        assertTrue(result < 0, "Should return negative when Date/Start match but EndTime is earlier");
    }

    @Test
    void shouldReturnPositive_WhenDateAndStartEqual_AndEndTimeLater() {
        var slotA = new TimeSlotValueObject(DATE, START, END.plusHours(1));
        var slotB = new TimeSlotValueObject(DATE, START, END);

        int result = comparator.compare(slotA, slotB);

        assertTrue(result > 0, "Should return positive when Date/Start match but EndTime is later");
    }

    @Test
    void shouldReturnZero_WhenAllFieldsAreEqual() {
        var slotA = new TimeSlotValueObject(DATE, START, END);
        var slotB = new TimeSlotValueObject(DATE, START, END);

        assertEquals(0, comparator.compare(slotA, slotB), "Should return 0 when all fields are identical");
    }

    @Test
    void shouldBeConsistentWithEquals() {
        var slotA = new TimeSlotValueObject(DATE, START, END);
        var slotB = new TimeSlotValueObject(DATE, START, END);

        assertEquals(0, comparator.compare(slotA, slotB));
        assertEquals(slotA, slotB);
    }
}
