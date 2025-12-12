package karsch.lukas.features.lectures.command.lookup.timeSlot;

import jakarta.persistence.*;
import karsch.lukas.features.lectures.command.lookup.assessment.TimeSlotEmbeddable;
import karsch.lukas.features.lectures.command.lookup.assessment.TimeSlotEmbeddableComparator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SortComparator;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

@Entity
@Table(
        name = "lecture_timeslots_lookup"
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LectureTimeslotLookupEntity {
    @Id
    private UUID id;

    @ElementCollection
    @CollectionTable(name = "lecture_timeslots", joinColumns = @JoinColumn(name = "lecture_id"))
    @SortComparator(TimeSlotEmbeddableComparator.class)
    private SortedSet<TimeSlotEmbeddable> timeSlots = new TreeSet<>(new TimeSlotEmbeddableComparator());

}
