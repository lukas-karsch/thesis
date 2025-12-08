package karsch.lukas.time;

import jakarta.persistence.Embeddable;

import java.time.LocalDate;
import java.time.LocalTime;

@Embeddable
public record TimeSlotValueObject(
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime) {
}
