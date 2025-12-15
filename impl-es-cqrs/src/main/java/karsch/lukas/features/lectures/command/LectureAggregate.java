package karsch.lukas.features.lectures.command;

import karsch.lukas.core.exceptions.DomainException;
import karsch.lukas.core.exceptions.NotAllowedException;
import karsch.lukas.features.course.commands.ICourseValidator;
import karsch.lukas.features.course.exceptions.MissingCoursesException;
import karsch.lukas.features.enrollment.command.lookup.credits.IStudentCreditsValidator;
import karsch.lukas.features.lectures.api.*;
import karsch.lukas.features.lectures.command.lookup.timeSlot.ITimeSlotValidator;
import karsch.lukas.features.professor.command.IProfessorValidator;
import karsch.lukas.features.student.command.lookup.IStudentValidator;
import karsch.lukas.features.student.command.lookup.StudentLookupEntity;
import karsch.lukas.lecture.LectureStatus;
import karsch.lukas.lecture.TimeSlot;
import karsch.lukas.time.DateTimeProvider;
import karsch.lukas.time.TimeSlotComparator;
import karsch.lukas.time.TimeSlotService;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateMember;
import org.axonframework.spring.stereotype.Aggregate;

import java.time.Instant;
import java.util.*;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
@Slf4j
public class LectureAggregate {
    public static final String PROCESSING_GROUP = "lecture_commands";

    @AggregateIdentifier
    private UUID id;

    private UUID courseId;

    private int maximumStudents;

    private SortedSet<TimeSlot> timeSlots;

    private List<UUID> enrolledStudents;

    private List<UUID> waitlistedStudents;

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
        if (command.lectureStatus() == this.lectureStatus) {
            return;
        }
        if (command.lectureStatus().ordinal() < this.lectureStatus.ordinal()) {
            throw new DomainException("Can not move lecture lifecycle backwards. Was " + this.lectureStatus + ", tried to set " + command.lectureStatus());
        }
        if (!command.professorId().equals(this.professorId)) {
            throw new NotAllowedException("Not allowed to advance lifecycle.");
        }
        apply(new LectureLifecycleAdvancedEvent(this.id, command.lectureStatus(), this.professorId));
        if (command.lectureStatus() == LectureStatus.IN_PROGRESS) {
            apply(new WaitlistClearedEvent(this.id, this.professorId));
        }
    }

    @CommandHandler
    public void handle(AddAssessmentCommand command, TimeSlotService timeSlotService) {
        assertProfessorIsAllowedToMakeChanges(command.professorId());

        if (this.assessments.containsKey(command.assessmentId())) {
            throw new DomainException("Assessment already exists.");
        }

        if (timeSlotService.isLive(command.timeSlot()) || timeSlotService.hasEnded(command.timeSlot())) {
            log.debug("Current system time is {}", timeSlotService.getCurrentTime());
            throw new DomainException("Can not add assessment. TimeSlot " + command.timeSlot() + " is in the past.");
        }

        apply(new AssessmentAddedEvent(
                command.lectureId(),
                command.assessmentId(),
                command.timeSlot(),
                command.assessmentType(),
                command.weight(),
                this.professorId
        ));
    }

    @CommandHandler
    public void handle(AssignTimeSlotsToLectureCommand command, TimeSlotService timeSlotService) {
        assertProfessorIsAllowedToMakeChanges(command.professorId());

        if (timeSlotService.containsOverlappingTimeslots(command.dates())) {
            throw new DomainException("New dates contain overlapping timeslots.");
        }

        var newSlots = new TreeSet<>(new TimeSlotComparator());
        newSlots.addAll(command.dates());

        if (newSlots.size() != command.dates().size()) {
            throw new DomainException("New timeSlots contained duplicates");
        }

        if (timeSlotService.areConflictingTimeSlots(this.timeSlots, newSlots)) {
            throw new DomainException("New time slots are conflicting with existing ones");
        }

        apply(new TimeSlotsAssignedEvent(this.id, newSlots.stream().toList(), this.professorId));
    }

    @CommandHandler
    public void handle(EnrollStudentCommand command,
                       DateTimeProvider dateTimeProvider,
                       IStudentValidator studentValidator,
                       ITimeSlotValidator timeSlotValidator,
                       IStudentCreditsValidator studentCreditsValidator
    ) {
        if (this.lectureStatus != LectureStatus.OPEN_FOR_ENROLLMENT) {
            throw new DomainException("Lecture " + this.id + " is not open for enrollment (" + this.lectureStatus + ")");
        }

        if (this.enrolledStudents.contains(command.studentId())) {
            throw new DomainException("Student " + command.studentId() + " is already enrolled to " + this.id);
        }

        if (!studentValidator.existsById(command.studentId())) {
            throw new DomainException("Student " + command.studentId() + " doesn't exist.");
        }

        if (timeSlotValidator.overlapsWithOtherLectures(this.timeSlots, command.studentId())) {
            throw new DomainException("Can not enroll in lectures with overlapping timeslots.");
        }

        if (!studentCreditsValidator.hasEnoughCreditsToEnroll(command.studentId(), this.courseId)) {
            throw new DomainException("Student has not enough credits to enroll");
        }

        if (this.enrolledStudents.size() >= this.maximumStudents) {
            apply(new StudentWaitlistedEvent(this.id, command.studentId(), Instant.now(dateTimeProvider.getClock())));
        } else {
            apply(new StudentEnrollmentApprovedEvent(this.id, command.studentId(), this.courseId));
        }
    }

    @CommandHandler
    public void handle(ConfirmStudentEnrollmentCommand command) {
        // sent by the Saga after confirming the enrollment
        apply(new StudentEnrolledEvent(command.lectureId(), command.studentId()));
    }

    @CommandHandler
    public void handle(DisenrollStudentCommand command, IStudentValidator studentValidator) {
        if (this.lectureStatus == LectureStatus.ARCHIVED || this.lectureStatus == LectureStatus.FINISHED) {
            log.debug("Disenrolling student {} from lecture {} has no effect because lectureStatus={}", command.studentId(), this.id, this.lectureStatus);
            return;
        }

        if (waitlistedStudents.contains(command.studentId())) {
            apply(new StudentRemovedFromWaitlistEvent(this.id, command.studentId()));
            return;
        }

        if (enrolledStudents.contains(command.studentId())) {
            apply(new StudentDisenrolledEvent(this.id, command.studentId()));
            findNextEligibleStudent(studentValidator)
                    .ifPresent(nextEligibleStudent -> {
                        log.debug("Next eligible student for lecture {} is {}", this.id, nextEligibleStudent);
                        apply(new StudentRemovedFromWaitlistEvent(this.id, nextEligibleStudent));
                        apply(new StudentEnrollmentApprovedEvent(this.id, nextEligibleStudent, this.courseId));
                    });
        }
    }

    private Optional<UUID> findNextEligibleStudent(IStudentValidator studentValidator) {
        if (this.waitlistedStudents.isEmpty()) {
            return Optional.empty();
        }

        List<StudentLookupEntity> waitlist = studentValidator.findByIds(this.waitlistedStudents);
        return waitlist
                .stream()
                .max(Comparator
                        .comparingInt(StudentLookupEntity::getSemester)
                        .thenComparing(s -> this.waitlistedStudents.indexOf(s.getId()) * -1)
                )
                .map(StudentLookupEntity::getId);
    }

    @EventSourcingHandler
    public void on(LectureCreatedEvent lectureCreatedEvent) {
        log.debug("handling {}", lectureCreatedEvent);
        this.id = lectureCreatedEvent.lectureId();
        this.courseId = lectureCreatedEvent.courseId();
        this.maximumStudents = lectureCreatedEvent.maximumStudents();
        this.timeSlots = new TreeSet<>(new TimeSlotComparator());
        timeSlots.addAll(lectureCreatedEvent.dates());
        this.enrolledStudents = new ArrayList<>();
        this.waitlistedStudents = new ArrayList<>();
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

    @EventSourcingHandler
    public void handle(TimeSlotsAssignedEvent event) {
        log.debug("handling {}", event);
        this.timeSlots.addAll(event.newTimeSlots());
    }

    @EventSourcingHandler
    public void handle(StudentEnrolledEvent event) {
        log.debug("handling {}", event);
        if (this.enrolledStudents.contains(event.studentId())) {
            return;
        }
        this.enrolledStudents.add(event.studentId());
    }

    @EventSourcingHandler
    public void handle(StudentWaitlistedEvent event) {
        log.debug("handling {}", event);
        if (this.waitlistedStudents.contains(event.studentId())) {
            return;
        }
        this.waitlistedStudents.add(event.studentId());
    }

    @EventSourcingHandler
    public void on(StudentDisenrolledEvent event) {
        log.debug("handling {}", event);
        enrolledStudents.remove(event.studentId());
    }

    @EventSourcingHandler
    public void on(StudentRemovedFromWaitlistEvent event) {
        log.debug("handling {}", event);
        waitlistedStudents.remove(event.studentId());
    }

    @EventSourcingHandler
    public void handle(WaitlistClearedEvent event) {
        log.debug("handling {}", event);

        this.waitlistedStudents.clear();
    }

    private void assertProfessorIsAllowedToMakeChanges(UUID requestingProfessorId) {
        if (!this.professorId.equals(requestingProfessorId)) {
            throw new NotAllowedException("Not allowed to add assessments.");
        }
    }

}
