package karsch.lukas.features.enrollment.command;

import karsch.lukas.features.enrollment.api.AssignGradeCommand;
import karsch.lukas.features.enrollment.api.EnrollmentCreatedEvent;
import karsch.lukas.features.enrollment.api.GradeAssignedEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
@Slf4j
public class EnrollmentAggregate {

    public static final String PROCESSING_GROUP = "enrollment_commands";

    @AggregateIdentifier
    private UUID id;

    private UUID studentId;

    private UUID lectureId;

    private Map<UUID, Integer> grades = new HashMap<>();

    private boolean areCreditsAwarded = false;

    public EnrollmentAggregate(UUID id, UUID studentId, UUID lectureId) {
        apply(new EnrollmentCreatedEvent(id, studentId, lectureId));
    }

    protected EnrollmentAggregate() {
    }

    public void handle(AssignGradeCommand command) {
        apply(new GradeAssignedEvent(this.id, command.assessmentId(), command.grade(), command.professorId()));
    }

    @EventHandler
    public void on(EnrollmentCreatedEvent event) {
        this.id = event.id();
        this.studentId = event.studentId();
        this.lectureId = event.lectureId();
    }

    @EventHandler
    public void on(GradeAssignedEvent event) {
        this.grades.put(event.assessmentId(), event.grade());
    }
}
