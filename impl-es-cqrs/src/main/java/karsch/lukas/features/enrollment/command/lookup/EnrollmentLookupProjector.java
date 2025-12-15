package karsch.lukas.features.enrollment.command.lookup;

import karsch.lukas.features.enrollment.api.EnrollmentCreatedEvent;
import karsch.lukas.features.enrollment.command.EnrollmentAggregate;
import lombok.RequiredArgsConstructor;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ProcessingGroup(EnrollmentAggregate.PROCESSING_GROUP)
public class EnrollmentLookupProjector {

    private final EnrollmentLookupRepository enrollmentLookupRepository;

    @EventHandler
    public void on(EnrollmentCreatedEvent event) {
        var entity = new EnrollmentLookupEntity(event.enrollmentId(), event.lectureId(), event.studentId());

        enrollmentLookupRepository.save(entity);
    }

}
