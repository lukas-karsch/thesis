package karsch.lukas.features.lectures.command;

import karsch.lukas.core.exceptions.DomainException;
import karsch.lukas.core.exceptions.NotAllowedException;
import karsch.lukas.features.course.commands.ICourseValidator;
import karsch.lukas.features.course.exceptions.MissingCoursesException;
import karsch.lukas.features.lectures.api.*;
import karsch.lukas.features.professor.command.IProfessorValidator;
import karsch.lukas.lecture.LectureStatus;
import karsch.lukas.lecture.TimeSlot;
import karsch.lukas.stats.AssessmentType;
import karsch.lukas.time.TimeSlotService;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LectureAggregateTest {

    private FixtureConfiguration<LectureAggregate> fixture;

    private ICourseValidator courseValidator;
    private TimeSlotService timeSlotService;
    private IProfessorValidator professorValidator;

    @BeforeEach
    void setup() {
        fixture = new AggregateTestFixture<>(LectureAggregate.class);

        courseValidator = mock(ICourseValidator.class);
        timeSlotService = mock(TimeSlotService.class);
        professorValidator = mock(IProfessorValidator.class);

        fixture.registerInjectableResource(courseValidator);
        fixture.registerInjectableResource(timeSlotService);
        fixture.registerInjectableResource(professorValidator);
    }

    @Test
    void testCreatingLecture_whenCourseNotExist() {
        when(courseValidator.courseExists(any())).thenReturn(false);
        when(timeSlotService.containsOverlappingTimeslots(anyCollection())).thenReturn(false);
        when(professorValidator.existsById(any())).thenReturn(true);

        fixture.givenNoPriorActivity()
                .when(new CreateLectureCommand(UUID.randomUUID(), UUID.randomUUID(), 1, List.of(), UUID.randomUUID()))
                .expectException(MissingCoursesException.class);
    }

    @Test
    void testCreatingLecture_whenOverlappingTimeslots() {
        when(courseValidator.courseExists(any())).thenReturn(true);
        when(timeSlotService.containsOverlappingTimeslots(anyCollection())).thenReturn(true);
        when(professorValidator.existsById(any())).thenReturn(true);

        fixture.givenNoPriorActivity()
                .when(new CreateLectureCommand(UUID.randomUUID(), UUID.randomUUID(), 1, List.of(), UUID.randomUUID()))
                .expectException(DomainException.class);
    }

    @Test
    void testCreatingLecture_whenProfessorNotExist() {
        when(courseValidator.courseExists(any())).thenReturn(true);
        when(timeSlotService.containsOverlappingTimeslots(anyCollection())).thenReturn(false);
        when(professorValidator.existsById(any())).thenReturn(false);

        fixture.givenNoPriorActivity()
                .when(new CreateLectureCommand(UUID.randomUUID(), UUID.randomUUID(), 1, List.of(), UUID.randomUUID()))
                .expectException(DomainException.class);
    }

    @Test
    void testCreatingLecture() {
        when(courseValidator.courseExists(any())).thenReturn(true);
        when(timeSlotService.containsOverlappingTimeslots(anyCollection())).thenReturn(false);
        when(professorValidator.existsById(any())).thenReturn(true);

        UUID lectureId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID professorId = UUID.randomUUID();
        fixture.givenNoPriorActivity()
                .when(new CreateLectureCommand(lectureId, courseId, 1, List.of(), professorId))
                .expectSuccessfulHandlerExecution()
                .expectEvents(
                        new LectureCreatedEvent(lectureId, courseId, 1, List.of(), professorId, LectureStatus.DRAFT)
                );
    }

    @Test
    void testAdvancingLifecycle_whenLectureStatusUnchanged() {
        final UUID lectureId = UUID.randomUUID();
        final UUID professorId = UUID.randomUUID();
        final LectureStatus draft = LectureStatus.DRAFT;

        fixture.given(new LectureCreatedEvent(lectureId, UUID.randomUUID(), 1, List.of(), professorId, draft))
                .when(new AdvanceLectureLifecycleCommand(lectureId, draft, professorId))
                .expectNoEvents();
    }

    @Test
    void testAdvancingLifecycle_whenTransitionIllegal() {
        final UUID lectureId = UUID.randomUUID();

        fixture.given(new LectureCreatedEvent(lectureId, UUID.randomUUID(), 1, List.of(), UUID.randomUUID(), LectureStatus.IN_PROGRESS))
                .when(new AdvanceLectureLifecycleCommand(lectureId, LectureStatus.DRAFT, UUID.randomUUID()))
                .expectException(DomainException.class);
    }

    @Test
    void testAdvancingLifecycle_whenWrongProfessor() {
        final UUID lectureId = UUID.randomUUID();

        fixture.given(new LectureCreatedEvent(lectureId, UUID.randomUUID(), 1, List.of(), UUID.randomUUID(), LectureStatus.IN_PROGRESS))
                .when(new AdvanceLectureLifecycleCommand(lectureId, LectureStatus.FINISHED, UUID.randomUUID()))
                .expectException(NotAllowedException.class);
    }

    @Test
    void testAdvancingLifecycleToInProgress_shouldClearWaitlist() {
        final UUID lectureId = UUID.randomUUID();
        final UUID professorId = UUID.randomUUID();

        fixture.given(new LectureCreatedEvent(lectureId, UUID.randomUUID(), 1, List.of(), professorId, LectureStatus.DRAFT))
                .when(new AdvanceLectureLifecycleCommand(lectureId, LectureStatus.IN_PROGRESS, professorId))
                .expectEvents(
                        new LectureLifecycleAdvancedEvent(lectureId, LectureStatus.IN_PROGRESS, professorId),
                        new WaitlistClearedEvent(lectureId, professorId)
                );
    }

    @Test
    void testAddingAssessment_shouldThrow_ifWrongProfessor() {
        final UUID lectureId = UUID.randomUUID();

        fixture.given(lectureCreatedEvent(lectureId))
                .when(new AddAssessmentCommand(lectureId, UUID.randomUUID(), anyTimeSlot(), AssessmentType.EXAM, 1, UUID.randomUUID()))
                .expectException(NotAllowedException.class);
    }

    @Test
    void testAddingAssessment_shouldThrow_ifInvalidTimeSlot() {
        final UUID lectureId = UUID.randomUUID();
        final UUID professorId = UUID.randomUUID();

        when(timeSlotService.isLive(any())).thenReturn(true);

        fixture.given(lectureCreatedEvent(lectureId, professorId))
                .when(new AddAssessmentCommand(lectureId, UUID.randomUUID(), anyTimeSlot(), AssessmentType.EXAM, 1, professorId))
                .expectException(DomainException.class);
    }

    @Test
    void testAddingAssessment_shouldEmitEvent() {
        final UUID lectureId = UUID.randomUUID();
        final UUID professorId = UUID.randomUUID();
        final UUID assessmentId = UUID.randomUUID();

        final TimeSlot timeSlot = anyTimeSlot();

        when(timeSlotService.isLive(any())).thenReturn(false);
        when(timeSlotService.hasEnded(any())).thenReturn(false);

        fixture.given(lectureCreatedEvent(lectureId, professorId))
                .when(new AddAssessmentCommand(lectureId, assessmentId, timeSlot, AssessmentType.EXAM, 1, professorId))
                .expectSuccessfulHandlerExecution()
                .expectEvents(
                        new AssessmentAddedEvent(lectureId, assessmentId, timeSlot, AssessmentType.EXAM, 1, professorId)
                );
    }

    private static LectureCreatedEvent lectureCreatedEvent(UUID lectureId, UUID professorId) {
        return new LectureCreatedEvent(lectureId, UUID.randomUUID(), 1, List.of(), professorId, LectureStatus.DRAFT);
    }

    private static LectureCreatedEvent lectureCreatedEvent(UUID lectureId) {
        return lectureCreatedEvent(lectureId, UUID.randomUUID());
    }

    private static TimeSlot anyTimeSlot() {
        return new TimeSlot(LocalDate.of(2025, 12, 1), LocalTime.of(10, 0), LocalTime.of(12, 0));
    }
}
