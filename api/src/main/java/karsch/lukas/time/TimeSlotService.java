package karsch.lukas.time;

import karsch.lukas.lecture.TimeSlot;
import karsch.lukas.mappers.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.SortedSet;

@Service
@RequiredArgsConstructor
public class TimeSlotService {

    private final DateTimeProvider dateTimeProvider;

    public boolean isLive(TimeSlot slot) {
        LocalDateTime now = getCurrentTime();
        LocalDateTime start = LocalDateTime.of(slot.date(), slot.startTime());
        LocalDateTime end = LocalDateTime.of(slot.date(), slot.endTime());

        return !now.isBefore(start) && now.isBefore(end);
    }

    public <T> boolean isLive(T slot, Mapper<T, TimeSlot> mapper) {
        return isLive(mapper.map(slot));
    }

    public boolean hasEnded(TimeSlot slot) {
        LocalDateTime now = getCurrentTime();
        LocalDateTime end = LocalDateTime.of(slot.date(), slot.endTime());

        return !now.isBefore(end);
    }

    public <T> boolean hasEnded(T slot, Mapper<T, TimeSlot> mapper) {
        return hasEnded(mapper.map(slot));
    }

    public boolean areOverlapping(TimeSlot slot1, TimeSlot slot2) {
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
    public boolean areConflictingTimeSlots(SortedSet<TimeSlot> slots1, SortedSet<TimeSlot> slots2) {
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

    public <T> boolean areConflictingTimeSlots(SortedSet<T> slots1, SortedSet<T> slots2, Mapper<T, TimeSlot> mapper) {
        var comparator = new TimeSlotComparator();
        return areConflictingTimeSlots(mapper.mapToSortedSet(slots1, comparator), mapper.mapToSortedSet(slots2, comparator));
    }

    public boolean containsOverlappingTimeslots(Collection<TimeSlot> timeSlots) {
        if (timeSlots.isEmpty()) {
            return false;
        }

        for (var timeSlotA : timeSlots) {
            for (var timeSlotB : timeSlots) {
                if (timeSlotA == timeSlotB) continue;
                if (!timeSlotA.date().equals(timeSlotB.date())) continue;

                if (areOverlapping(timeSlotA, timeSlotB))
                    return true;
            }
        }

        return false;
    }

    public <T> boolean containsOverlappingTimeslots(Collection<T> timeSlots, Mapper<T, TimeSlot> mapper) {
        return containsOverlappingTimeslots(mapper.mapToList(timeSlots));
    }

    private LocalDateTime getCurrentTime() {
        return LocalDateTime.now(dateTimeProvider.getClock());
    }

}
