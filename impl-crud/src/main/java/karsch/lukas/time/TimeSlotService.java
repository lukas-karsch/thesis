package karsch.lukas.time;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.SortedSet;

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

    public boolean areOverlapping(TimeSlotValueObject slot1, TimeSlotValueObject slot2) {
        if (!slot1.date().equals(slot2.date())) {
            return false;
        }

        var startTime1 = slot1.startTime();
        var startTime2 = slot2.startTime();

        var endTime1 = slot1.endTime();
        var endTime2 = slot2.endTime();

        if (startTime1.isAfter(endTime2) || startTime2.isAfter(endTime1)) {
            return false;
        }

        return true;
    }

    /**
     * Optimized function to check overlaps in two sorted sets.
     *
     * @return True if any time slots overlap
     */
    public boolean areConflictingTimeSlots(SortedSet<TimeSlotValueObject> slots1, SortedSet<TimeSlotValueObject> slots2) {
        var iterator1 = slots1.iterator();
        var iterator2 = slots2.iterator();

        if (!iterator1.hasNext() || !iterator2.hasNext()) {
            return false;
        }

        var slotA = iterator1.next();
        var slotB = iterator2.next();

        while (true) {
            int dateCompare = slotA.date().compareTo(slotB.date());
            if (dateCompare == 0) {
                if (areOverlapping(slotA, slotB))
                    return true;
            }

            boolean aEndsEarlier =
                    slotA.date().isBefore(slotB.date()) ||
                            (slotA.date().equals(slotB.date()) && slotA.endTime().isBefore(slotB.endTime()));

            if (aEndsEarlier) {
                if (!iterator1.hasNext())
                    return false;
                slotA = iterator1.next();
            } else {
                if (!iterator2.hasNext())
                    return false;
                slotB = iterator2.next();
            }
        }
    }

    private LocalDateTime getCurrentTime() {
        return LocalDateTime.now(dateTimeProvider.getClock());
    }

}
