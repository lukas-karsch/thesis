package karsch.lukas.features.lectures.command;

import karsch.lukas.core.exceptions.DomainException;
import karsch.lukas.core.exceptions.NotAllowedException;
import karsch.lukas.features.course.commands.ICourseValidator;
import karsch.lukas.features.course.exceptions.MissingCoursesException;
import karsch.lukas.features.lectures.api.*;
import karsch.lukas.features.professor.command.IProfessorValidator;
import karsch.lukas.lecture.LectureStatus;
import karsch.lukas.time.TimeSlotService;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
