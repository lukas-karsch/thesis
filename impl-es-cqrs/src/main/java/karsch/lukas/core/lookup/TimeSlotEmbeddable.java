package karsch.lukas.core.lookup;

import jakarta.persistence.Embeddable;

import java.time.LocalDate;
import java.time.LocalTime;

@Embeddable
public record TimeSlotEmbeddable(LocalDate date, LocalTime startTime, LocalTime endTime) {
}
