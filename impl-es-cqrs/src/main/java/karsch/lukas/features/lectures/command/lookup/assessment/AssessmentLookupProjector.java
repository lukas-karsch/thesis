package karsch.lukas.features.lectures.command.lookup.assessment;

import karsch.lukas.features.lectures.api.AssessmentAddedEvent;
import karsch.lukas.features.lectures.command.LectureAggregate;
import lombok.RequiredArgsConstructor;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ProcessingGroup(LectureAggregate.PROCESSING_GROUP)
public class AssessmentLookupProjector {

    private final AssessmentLookupRepository assessmentLookupRepository;

    @EventHandler
    public void on(AssessmentAddedEvent event) {
        var entity = new AssessmentLookupEntity(
                event.assessmentId(),
                event.lectureId(),
                event.weight(),
                new TimeSlotEmbeddable(
                        event.timeSlot().date(),
                        event.timeSlot().startTime(),
                        event.timeSlot().endTime()
                ),
                event.assessmentType()
        );

        assessmentLookupRepository.save(entity);
    }

}
