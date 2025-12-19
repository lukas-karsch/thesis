package karsch.lukas.features.enrollment.command;

import karsch.lukas.features.enrollment.api.*;
import karsch.lukas.features.enrollment.exception.MissingGradeException;
import karsch.lukas.features.lectures.api.CreateEnrollmentCommand;
import karsch.lukas.time.TimeSlotService;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.HashMap;
import java.util.List;
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

    private UUID courseId;

    /**
     * maps assessment IDs to grades
     */
    private final Map<UUID, Integer> grades = new HashMap<>();

    private boolean areCreditsAwarded = false;

    private boolean hasPassed = false;

    @CommandHandler
    public EnrollmentAggregate(CreateEnrollmentCommand command) {
        apply(new EnrollmentCreatedEvent(command.enrollmentId(), command.studentId(), command.lectureId(), command.courseId()));
    }

    protected EnrollmentAggregate() {
    }

    public void handle(AssignGradeCommand command, TimeSlotService timeSlotService) {
        apply(new GradeAssignedEvent(this.id, command.assessmentId(), command.grade(), command.professorId(), this.studentId, timeSlotService.getCurrentTime()));
    }

    public void handle(UpdateGradeCommand command, TimeSlotService timeSlotService) {
        if (!this.grades.containsKey(command.assessmentId())) {
            throw new MissingGradeException(command.assessmentId(), command.studentId());
        }

        apply(new GradeUpdatedEvent(this.id, command.assessmentId(), command.grade(), command.professorId(), this.studentId, timeSlotService.getCurrentTime()));
    }

    @CommandHandler
    public void handle(AwardCreditsCommand command) {
        if (this.areCreditsAwarded) {
            return;
        }
        boolean hasPassed = calculateHasPassed(command.allAssessmentIds());
        apply(new CreditsAwardedEvent(this.id, this.studentId, hasPassed, this.courseId));
    }

    private boolean calculateHasPassed(List<UUID> allAssessmentIds) {
        return this.grades.keySet().containsAll(allAssessmentIds) && this.grades.values().stream().allMatch(g -> g >= 50);
    }

    @EventHandler
    public void on(EnrollmentCreatedEvent event) {
        this.id = event.enrollmentId();
        this.studentId = event.studentId();
        this.lectureId = event.lectureId();
        this.courseId = event.courseId();
    }

    @EventHandler
    public void on(GradeAssignedEvent event) {
        this.grades.put(event.assessmentId(), event.grade());
    }

    @EventHandler
    public void on(GradeUpdatedEvent event) {
        this.grades.put(event.assessmentId(), event.grade());
    }

    @EventHandler
    public void on(CreditsAwardedEvent event) {
        this.areCreditsAwarded = true;
        this.hasPassed = event.hasPassed();
    }

}
