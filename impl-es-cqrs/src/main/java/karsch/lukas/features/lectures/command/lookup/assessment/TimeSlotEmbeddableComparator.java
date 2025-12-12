package karsch.lukas.features.lectures.command.lookup.assessment;

import java.util.Comparator;

// TODO: move

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
