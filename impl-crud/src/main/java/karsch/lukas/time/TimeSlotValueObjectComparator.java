package karsch.lukas.time;

import java.util.Comparator;

public class TimeSlotValueObjectComparator implements Comparator<TimeSlotValueObject> {

    @Override
    public int compare(TimeSlotValueObject a, TimeSlotValueObject b) {
        int cmp = a.date().compareTo(b.date());
        if (cmp != 0) return cmp;

        cmp = a.startTime().compareTo(b.startTime());
        if (cmp != 0) return cmp;

        return a.endTime().compareTo(b.endTime());
    }
}
