package karsch.lukas.features.lectures.command;

import karsch.lukas.core.exceptions.DomainException;
import karsch.lukas.core.exceptions.NotAllowedException;
import karsch.lukas.features.course.commands.ICourseValidator;
import karsch.lukas.features.course.exceptions.MissingCoursesException;
import karsch.lukas.features.lectures.api.AdvanceLectureLifecycleCommand;
import karsch.lukas.features.lectures.api.CreateLectureCommand;
import karsch.lukas.features.lectures.api.LectureCreatedEvent;
import karsch.lukas.features.lectures.api.LectureLifecycleAdvancedEvent;
import karsch.lukas.features.professor.command.IProfessorValidator;
import karsch.lukas.lecture.LectureStatus;
import karsch.lukas.lecture.TimeSlot;
import karsch.lukas.time.TimeSlotService;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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

    private List<Object> assessments; // TODO

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

    @EventSourcingHandler
    public void on(LectureCreatedEvent lectureCreatedEvent) {
        log.debug("handling {}", lectureCreatedEvent);
        this.id = lectureCreatedEvent.id();
        this.courseId = lectureCreatedEvent.courseId();
        this.maximumStudents = lectureCreatedEvent.maximumStudents();
        this.dates = lectureCreatedEvent.dates(); // TODO needs to be sorted
        this.enrolledStudents = new ArrayList<>();
        this.assessments = new ArrayList<>();
        this.lectureStatus = lectureCreatedEvent.lectureStatus();
        this.professorId = lectureCreatedEvent.professorId();
    }

    @EventSourcingHandler
    public void on(LectureLifecycleAdvancedEvent event) {
        log.debug("handling {}", event);
        this.lectureStatus = event.lectureStatus();
    }

}
