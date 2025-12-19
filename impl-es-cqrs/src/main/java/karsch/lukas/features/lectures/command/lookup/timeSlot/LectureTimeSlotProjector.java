package karsch.lukas.features.lectures.command.lookup.timeSlot;

import jakarta.transaction.Transactional;
import karsch.lukas.core.lookup.TimeSlotEmbeddable;
import karsch.lukas.core.lookup.TimeSlotEmbeddableComparator;
import karsch.lukas.features.lectures.api.LectureCreatedEvent;
import karsch.lukas.features.lectures.api.TimeSlotsAssignedEvent;
import karsch.lukas.features.lectures.command.LectureAggregate;
import karsch.lukas.lecture.TimeSlot;
import lombok.RequiredArgsConstructor;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Component
@ProcessingGroup(LectureAggregate.PROCESSING_GROUP)
@RequiredArgsConstructor
class LectureTimeSlotProjector {
    private final LectureTimeslotLookupRepository lectureTimeslotLookupRepository;

    @EventHandler
    public void on(LectureCreatedEvent event) {
        var entity = new LectureTimeslotLookupEntity(
                event.lectureId(),
                toSortedSet(event.dates())
        );
        lectureTimeslotLookupRepository.save(entity);
    }

    @EventHandler
    @Transactional
    @Retryable
    public void on(TimeSlotsAssignedEvent event) {
        var entity = lectureTimeslotLookupRepository.findById(event.lectureId()).orElseThrow();
        entity.getTimeSlots().addAll(toSortedSet(event.newTimeSlots()));
        lectureTimeslotLookupRepository.save(entity);
    }

    private SortedSet<TimeSlotEmbeddable> toSortedSet(List<TimeSlot> timeSlots) {
        return timeSlots
                .stream()
                .map(d -> new TimeSlotEmbeddable(d.date(), d.startTime(), d.endTime()))
                .collect(Collectors.toCollection(() -> new TreeSet<>(new TimeSlotEmbeddableComparator())));
    }

}
