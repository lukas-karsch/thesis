package karsch.lukas.core.lookup;

import java.util.Comparator;

public class TimeSlotEmbeddableComparator implements Comparator<TimeSlotEmbeddable> {

    @Override
    public int compare(TimeSlotEmbeddable a, TimeSlotEmbeddable b) {
        int cmp = a.date().compareTo(b.date());
        if (cmp != 0) return cmp;

        cmp = a.startTime().compareTo(b.startTime());
        if (cmp != 0) return cmp;

        return a.endTime().compareTo(b.endTime());

    }

}
