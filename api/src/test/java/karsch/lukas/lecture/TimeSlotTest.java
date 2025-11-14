package karsch.lukas.lecture;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimeSlotTest {
    @Test
    void testIsValidTimeslot() {
        var dateNow = LocalDate.now();
        var timeSlot = new TimeSlot(LocalDate.now(), LocalTime.now().minus(Duration.ofHours(1)), LocalTime.now());
        assertThat(timeSlot.date()).isEqualTo(dateNow);
    }

    @Test
    void testInvalidTimeslot() {
        assertThatThrownBy(() -> new TimeSlot(
                LocalDate.now(), LocalTime.now(), LocalTime.now().minus(Duration.ofHours(1)))
        ).isInstanceOf(IllegalArgumentException.class);
    }
}
