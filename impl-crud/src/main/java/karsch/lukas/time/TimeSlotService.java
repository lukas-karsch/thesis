package karsch.lukas.time;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TimeSlotService {

    private final DateTimeProvider dateTimeProvider;

    public boolean isLive(TimeSlotValueObject slot) {
        LocalDateTime now = getCurrentTime();
        LocalDateTime start = LocalDateTime.of(slot.date(), slot.startTime());
        LocalDateTime end = LocalDateTime.of(slot.date(), slot.endTime());

        return !now.isBefore(start) && now.isBefore(end);
    }

    public boolean hasEnded(TimeSlotValueObject slot) {
        LocalDateTime now = getCurrentTime();
        LocalDateTime end = LocalDateTime.of(slot.date(), slot.endTime());

        return !now.isBefore(end);
    }

    private LocalDateTime getCurrentTime() {
        return LocalDateTime.now(dateTimeProvider.getClock());
    }

}
