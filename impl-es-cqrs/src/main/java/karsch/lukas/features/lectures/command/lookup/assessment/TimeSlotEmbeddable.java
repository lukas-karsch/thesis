package karsch.lukas.features.lectures.command.lookup.assessment;

import jakarta.persistence.Embeddable;

import java.time.LocalDate;
import java.time.LocalTime;

// TODO: move

@Embeddable
public record TimeSlotEmbeddable(LocalDate date, LocalTime startTime, LocalTime endTime) {
}
