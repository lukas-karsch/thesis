package karsch.lukas.lecture;

import java.time.LocalDate;
import java.time.LocalTime;

public record TimeSlot(LocalDate date, LocalTime startTime, LocalTime endTime) {
    public TimeSlot {
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("startTime must be earlier than endTime");
        }
    }
}
