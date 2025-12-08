package karsch.lukas.features.lectures.command;

import karsch.lukas.core.exceptions.DomainException;
import karsch.lukas.core.exceptions.NotAllowedException;
import karsch.lukas.features.course.commands.ICourseValidator;
import karsch.lukas.features.course.exceptions.MissingCoursesException;
import karsch.lukas.features.lectures.api.*;
import karsch.lukas.features.professor.command.IProfessorValidator;
import karsch.lukas.lecture.LectureStatus;
import karsch.lukas.lecture.TimeSlot;
import karsch.lukas.time.TimeSlotService;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateMember;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.*;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
@Slf4j
class LectureAggregate {
    @AggregateIdentifier
    private UUID id;

    private UUID courseId;

    private int maximumStudents;

    private List<TimeSlot> dates;

    private List<UUID> enrolledStudents;

    @AggregateMember
    private Map<UUID, AssessmentValueObject> assessments;

    private LectureStatus lectureStatus;

    private UUID professorId;

    @CommandHandler
    public LectureAggregate(CreateLectureCommand command, ICourseValidator courseValidator, TimeSlotService timeSlotService, IProfessorValidator professorValidator) {
        if (!courseValidator.courseExists(command.courseId())) {
            throw new MissingCoursesException(Collections.singleton(command.courseId()));
        }

        if (timeSlotService.containsOverlappingTimeslots(command.dates())) {
            throw new DomainException("Lecture contains overlapping timeslots.");
        }

        if (!professorValidator.existsById(command.professorId())) {
            throw new DomainException("Professor " + command.professorId() + " doesn't exist.");
        }

        apply(
                new LectureCreatedEvent(
                        command.id(),
                        command.courseId(),
                        command.maximumStudents(),
                        command.dates(),
                        command.professorId(),
                        LectureStatus.DRAFT
                )
        );
    }

    protected LectureAggregate() {
    }

    @CommandHandler
    public void handle(AdvanceLectureLifecycleCommand command) {
        if (command.lectureStatus().ordinal() < this.lectureStatus.ordinal()) {
            throw new DomainException("Can not move lecture lifecycle backwards. Was " + this.lectureStatus + ", tried to set " + command.lectureStatus());
        }
        if (!command.professorId().equals(this.professorId)) {
            throw new NotAllowedException("Not allowed to advance lifecycle.");
        }
        apply(new LectureLifecycleAdvancedEvent(this.id, command.lectureStatus()));
    }

    @CommandHandler
    public void handle(AddAssessmentCommand command) {
        if (!command.professorId().equals(this.professorId)) {
            throw new NotAllowedException("Not allowed to add assessments.");
        }

        if (this.assessments.containsKey(command.assessmentId())) {
            throw new DomainException("Assessment already exists.");
        }

        apply(new AssessmentAddedEvent(
                command.lectureId(),
                command.assessmentId(),
                command.timeSlot(),
                command.assessmentType(),
                command.weight()
        ));
    }

    @EventSourcingHandler
    public void on(LectureCreatedEvent lectureCreatedEvent) {
        log.debug("handling {}", lectureCreatedEvent);
        this.id = lectureCreatedEvent.lectureId();
        this.courseId = lectureCreatedEvent.courseId();
        this.maximumStudents = lectureCreatedEvent.maximumStudents();
        this.dates = lectureCreatedEvent.dates(); // TODO needs to be sorted
        this.enrolledStudents = new ArrayList<>();
        this.assessments = new HashMap<>();
        this.lectureStatus = lectureCreatedEvent.lectureStatus();
        this.professorId = lectureCreatedEvent.professorId();
    }

    @EventSourcingHandler
    public void on(LectureLifecycleAdvancedEvent event) {
        log.debug("handling {}", event);
        this.lectureStatus = event.lectureStatus();
    }

    @EventSourcingHandler
    public void on(AssessmentAddedEvent event) {
        log.debug("handling {}", event);
        this.assessments.put(event.assessmentId(), new AssessmentValueObject(
                event.assessmentId(),
                event.timeSlot(),
                event.weight(),
                event.assessmentType()
        ));
    }

}
