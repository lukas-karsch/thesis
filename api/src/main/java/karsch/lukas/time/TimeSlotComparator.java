package karsch.lukas.time;

import karsch.lukas.lecture.TimeSlot;

import java.util.Comparator;

public class TimeSlotComparator implements Comparator<TimeSlot> {

    @Override
    public int compare(TimeSlot a, TimeSlot b) {
        int cmp = a.date().compareTo(b.date());
        if (cmp != 0) return cmp;

        cmp = a.startTime().compareTo(b.startTime());
        if (cmp != 0) return cmp;

        return a.endTime().compareTo(b.endTime());

    }
}
