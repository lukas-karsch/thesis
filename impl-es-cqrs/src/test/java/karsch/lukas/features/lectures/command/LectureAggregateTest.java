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
import karsch.lukas.stats.AssessmentType;
import karsch.lukas.time.DateTimeProvider;
import karsch.lukas.time.TimeSlotService;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.*;
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
    private DateTimeProvider dateTimeProvider;
    private IStudentValidator studentValidator;
    private IStudentCreditsValidator studentCreditsValidator;
    private ITimeSlotValidator timeSlotValidator;


    @BeforeEach
    void setup() {
        fixture = new AggregateTestFixture<>(LectureAggregate.class);

        courseValidator = mock(ICourseValidator.class);
        timeSlotService = mock(TimeSlotService.class);
        professorValidator = mock(IProfessorValidator.class);
        dateTimeProvider = mock(DateTimeProvider.class);
        studentValidator = mock(IStudentValidator.class);
        studentCreditsValidator = mock(IStudentCreditsValidator.class);
        timeSlotValidator = mock(ITimeSlotValidator.class);

        fixture.registerInjectableResource(courseValidator);
        fixture.registerInjectableResource(timeSlotService);
        fixture.registerInjectableResource(professorValidator);
        fixture.registerInjectableResource(dateTimeProvider);
        fixture.registerInjectableResource(studentValidator);
        fixture.registerInjectableResource(studentCreditsValidator);
        fixture.registerInjectableResource(timeSlotValidator);
    }

    @Nested
    class CreateLectureTests {
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
    }

    @Nested
    class AdvanceLifecycleTests {
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

    @Nested
    class AddAssessmentTests {
        final UUID lectureId = UUID.randomUUID();

        @Test
        void testAddingAssessment_shouldThrow_ifWrongProfessor() {
            fixture.given(lectureCreatedEvent(lectureId))
                    .when(new AddAssessmentCommand(lectureId, UUID.randomUUID(), anyTimeSlot(), AssessmentType.EXAM, 1, UUID.randomUUID()))
                    .expectException(NotAllowedException.class);
        }

        @Test
        void testAddingAssessment_shouldThrow_ifAssessmentExists() {
            final UUID professorId = UUID.randomUUID();
            final UUID assessmentId = UUID.randomUUID();

            fixture.given(
                            lectureCreatedEvent(lectureId, professorId),
                            new AssessmentAddedEvent(lectureId, assessmentId, anyTimeSlot(), AssessmentType.EXAM, 1, professorId)
                    )
                    .when(new AddAssessmentCommand(lectureId, assessmentId, anyTimeSlot(), AssessmentType.EXAM, 1, professorId))
                    .expectException(DomainException.class);
        }

        @Test
        void testAddingAssessment_shouldThrow_ifInvalidTimeSlot() {
            final UUID professorId = UUID.randomUUID();

            when(timeSlotService.isLive(any())).thenReturn(true);

            fixture.given(lectureCreatedEvent(lectureId, professorId))
                    .when(new AddAssessmentCommand(lectureId, UUID.randomUUID(), anyTimeSlot(), AssessmentType.EXAM, 1, professorId))
                    .expectException(DomainException.class);
        }

        @Test
        void testAddingAssessment_shouldThrow_ifTimeSlotHasEnded() {
            final UUID professorId = UUID.randomUUID();

            when(timeSlotService.isLive(any())).thenReturn(false);
            when(timeSlotService.hasEnded(any())).thenReturn(true);

            fixture.given(lectureCreatedEvent(lectureId, professorId))
                    .when(new AddAssessmentCommand(lectureId, UUID.randomUUID(), anyTimeSlot(), AssessmentType.EXAM, 1, professorId))
                    .expectException(DomainException.class);
        }


        @Test
        void testAddingAssessment_shouldEmitEvent() {
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
    }


    @Nested
    class AssignTimeSlotsToLectureTests {
        private final UUID lectureId = UUID.randomUUID();
        private final UUID professorId = UUID.randomUUID();
        private final List<TimeSlot> newTimeSlots = List.of(anyTimeSlot());

        @Test
        void testAssignTimeSlots_shouldThrow_ifWrongProfessor() {
            fixture.given(lectureCreatedEvent(lectureId, professorId))
                    .when(new AssignTimeSlotsToLectureCommand(lectureId, newTimeSlots, UUID.randomUUID()))
                    .expectException(NotAllowedException.class);
        }

        @Test
        void testAssignTimeSlots_shouldThrow_ifNewDatesOverlap() {
            when(timeSlotService.containsOverlappingTimeslots(newTimeSlots)).thenReturn(true);

            fixture.given(lectureCreatedEvent(lectureId, professorId))
                    .when(new AssignTimeSlotsToLectureCommand(lectureId, newTimeSlots, professorId))
                    .expectException(DomainException.class);
        }

        @Test
        void testAssignTimeSlots_shouldThrow_ifNewDatesContainDuplicates() {
            var duplicatedTimeSlots = List.of(anyTimeSlot(), anyTimeSlot());
            when(timeSlotService.containsOverlappingTimeslots(duplicatedTimeSlots)).thenReturn(false);

            fixture.given(lectureCreatedEvent(lectureId, professorId))
                    .when(new AssignTimeSlotsToLectureCommand(lectureId, duplicatedTimeSlots, professorId))
                    .expectException(DomainException.class);
        }

        @Test
        void testAssignTimeSlots_shouldThrow_ifConflictingWithExisting() {
            when(timeSlotService.containsOverlappingTimeslots(newTimeSlots)).thenReturn(false);
            when(timeSlotService.areConflictingTimeSlots(any(), any())).thenReturn(true);

            fixture.given(lectureCreatedEvent(lectureId, professorId))
                    .when(new AssignTimeSlotsToLectureCommand(lectureId, newTimeSlots, professorId))
                    .expectException(DomainException.class);
        }

        @Test
        void testAssignTimeSlots_shouldEmitEvent() {
            when(timeSlotService.containsOverlappingTimeslots(newTimeSlots)).thenReturn(false);
            when(timeSlotService.areConflictingTimeSlots(any(), any())).thenReturn(false);

            fixture.given(lectureCreatedEvent(lectureId, professorId))
                    .when(new AssignTimeSlotsToLectureCommand(lectureId, newTimeSlots, professorId))
                    .expectSuccessfulHandlerExecution()
                    .expectEvents(new TimeSlotsAssignedEvent(lectureId, newTimeSlots, professorId));
        }
    }

    @Nested
    class EnrollStudentTests {

        private final UUID courseId = UUID.randomUUID();
        private final UUID lectureId = UUID.randomUUID();
        private final UUID professorId = UUID.randomUUID();

        @BeforeEach
        void localSetup() {
            when(studentValidator.existsById(any())).thenReturn(true);
            when(timeSlotValidator.overlapsWithOtherLectures(any(), any())).thenReturn(false);
        }

        @Test
        void testEnrollStudent_shouldThrow_ifLectureNotOpenForEnrollment() {
            when(studentCreditsValidator.hasPassedAllPrerequisites(any(), any())).thenReturn(true);
            when(studentCreditsValidator.hasEnoughCreditsToEnroll(any(), any())).thenReturn(true);

            fixture.given(lectureCreatedEvent(lectureId, professorId))
                    .when(new EnrollStudentCommand(lectureId, UUID.randomUUID()))
                    .expectException(DomainException.class);
        }

        @Test
        void testEnrollStudent_shouldThrow_ifStudentAlreadyEnrolled() {
            when(studentCreditsValidator.hasPassedAllPrerequisites(any(), any())).thenReturn(true);
            when(studentCreditsValidator.hasEnoughCreditsToEnroll(any(), any())).thenReturn(true);

            var studentId = UUID.randomUUID();
            fixture.given(
                            lectureCreatedEvent(lectureId, professorId),
                            new LectureLifecycleAdvancedEvent(lectureId, LectureStatus.OPEN_FOR_ENROLLMENT, professorId),
                            new StudentEnrolledEvent(lectureId, studentId)
                    )
                    .when(new EnrollStudentCommand(lectureId, studentId))
                    .expectException(DomainException.class);
        }

        @Test
        void testEnrollStudent_shouldThrow_ifStudentNotExist() {
            when(studentValidator.existsById(any())).thenReturn(false);

            fixture.given(
                            lectureCreatedEvent(lectureId, professorId),
                            new LectureLifecycleAdvancedEvent(lectureId, LectureStatus.OPEN_FOR_ENROLLMENT, professorId)
                    )
                    .when(new EnrollStudentCommand(lectureId, UUID.randomUUID()))
                    .expectException(DomainException.class);
        }

        @Test
        void testEnrollStudent_shouldThrow_ifOverlappingLectures() {
            when(timeSlotValidator.overlapsWithOtherLectures(any(), any())).thenReturn(true);

            when(studentCreditsValidator.hasPassedAllPrerequisites(any(), any())).thenReturn(true);
            when(studentCreditsValidator.hasEnoughCreditsToEnroll(any(), any())).thenReturn(true);

            fixture.given(
                            lectureCreatedEvent(lectureId, professorId),
                            new LectureLifecycleAdvancedEvent(lectureId, LectureStatus.OPEN_FOR_ENROLLMENT, professorId)
                    )
                    .when(new EnrollStudentCommand(lectureId, UUID.randomUUID()))
                    .expectException(DomainException.class);
        }

        @Test
        void testEnrollStudent_shouldWaitlistStudent_ifLectureIsFull() {
            var studentToEnroll = UUID.randomUUID();
            var enrolledStudent = UUID.randomUUID();
            var now = Instant.now();

            when(dateTimeProvider.getClock()).thenReturn(Clock.fixed(now, ZoneId.systemDefault()));

            when(studentCreditsValidator.hasEnoughCreditsToEnroll(any(), any())).thenReturn(true);
            when(studentCreditsValidator.hasPassedAllPrerequisites(any(), any())).thenReturn(true);

            fixture.given(
                            new LectureCreatedEvent(lectureId, UUID.randomUUID(), 1, List.of(), professorId, LectureStatus.DRAFT),
                            new LectureLifecycleAdvancedEvent(lectureId, LectureStatus.OPEN_FOR_ENROLLMENT, professorId),
                            new StudentEnrolledEvent(lectureId, enrolledStudent)
                    )
                    .when(new EnrollStudentCommand(lectureId, studentToEnroll))
                    .expectEvents(new StudentWaitlistedEvent(lectureId, studentToEnroll, now));
        }

        @Test
        void testEnrollStudent_shouldApproveEnrollment_ifLectureIsNotFull() {
            var studentId = UUID.randomUUID();

            when(studentCreditsValidator.hasEnoughCreditsToEnroll(any(), any())).thenReturn(true);
            when(studentCreditsValidator.hasPassedAllPrerequisites(any(), any())).thenReturn(true);

            fixture.given(
                            new LectureCreatedEvent(lectureId, courseId, 1, List.of(), professorId, LectureStatus.DRAFT),
                            new LectureLifecycleAdvancedEvent(lectureId, LectureStatus.OPEN_FOR_ENROLLMENT, professorId)
                    )
                    .when(new EnrollStudentCommand(lectureId, studentId))
                    .expectSuccessfulHandlerExecution()
                    .expectEvents(
                            new StudentEnrollmentApprovedEvent(lectureId, studentId, courseId)
                    );
        }

        @Test
        void testEnrollStudent_shouldEnrollStudent_onConfirmation() {
            // when the saga confirms the enrollment
            var studentId = UUID.randomUUID();

            fixture.given(
                            new LectureCreatedEvent(lectureId, courseId, 1, List.of(), professorId, LectureStatus.DRAFT),
                            new AdvanceLectureLifecycleCommand(lectureId, LectureStatus.OPEN_FOR_ENROLLMENT, professorId),
                            new StudentEnrollmentApprovedEvent(lectureId, studentId, courseId)
                    )
                    .when(new ConfirmStudentEnrollmentCommand(lectureId, studentId))
                    .expectSuccessfulHandlerExecution()
                    .expectEvents(new StudentEnrolledEvent(lectureId, studentId));
        }
    }

    @Nested
    class DisenrollStudentTests {
        private final UUID courseId = UUID.randomUUID();
        private final UUID lectureId = UUID.randomUUID();
        private final UUID professorId = UUID.randomUUID();

        @Test
        void testDisenrollStudent_shouldDoNothing_ifLectureArchived() {
            var studentId = UUID.randomUUID();
            fixture.given(
                            lectureCreatedEvent(lectureId, professorId),
                            new LectureLifecycleAdvancedEvent(lectureId, LectureStatus.ARCHIVED, professorId),
                            new StudentEnrolledEvent(lectureId, studentId)
                    )
                    .when(new DisenrollStudentCommand(lectureId, studentId))
                    .expectNoEvents();
        }

        @Test
        void testDisenrollStudent_shouldDoNothing_ifLectureFinished() {
            var studentId = UUID.randomUUID();
            fixture.given(
                            lectureCreatedEvent(lectureId, professorId),
                            new LectureLifecycleAdvancedEvent(lectureId, LectureStatus.FINISHED, professorId),
                            new StudentEnrolledEvent(lectureId, studentId)
                    )
                    .when(new DisenrollStudentCommand(lectureId, studentId))
                    .expectNoEvents();
        }

        @Test
        void testDisenrollStudent_shouldRemoveFromWaitlist_ifStudentIsOnWaitlist() {
            var studentId = UUID.randomUUID();
            var now = Instant.now();
            when(dateTimeProvider.getClock()).thenReturn(Clock.fixed(now, ZoneId.systemDefault()));

            fixture.given(
                            lectureCreatedEvent(lectureId, professorId),
                            new StudentWaitlistedEvent(lectureId, studentId, now)
                    )
                    .when(new DisenrollStudentCommand(lectureId, studentId))
                    .expectEvents(new StudentRemovedFromWaitlistEvent(lectureId, studentId));
        }

        @Test
        void testDisenrollStudent_shouldDisenroll_ifStudentIsEnrolledAndWaitlistIsEmpty() {
            var studentId = UUID.randomUUID();

            fixture.given(
                            lectureCreatedEvent(lectureId, professorId),
                            new StudentEnrolledEvent(lectureId, studentId)
                    )
                    .when(new DisenrollStudentCommand(lectureId, studentId))
                    .expectEvents(new StudentDisenrolledEvent(lectureId, studentId));
        }

        @Test
        void testDisenrollStudent_shouldDisenrollAndEnrollNext_ifStudentIsEnrolledAndWaitlistIsNotEmpty() {
            var studentToDisenroll = UUID.randomUUID();
            var studentOnWaitlist1 = UUID.randomUUID(); // lower semester
            var studentOnWaitlist2 = UUID.randomUUID(); // higher semester, should be chosen

            var now1 = Instant.now();
            var now2 = now1.plusSeconds(1);

            var student1Lookup = new StudentLookupEntity(studentOnWaitlist1, 3);
            var student2Lookup = new StudentLookupEntity(studentOnWaitlist2, 5);
            when(studentValidator.findByIds(List.of(studentOnWaitlist1, studentOnWaitlist2))).thenReturn(List.of(student1Lookup, student2Lookup));

            fixture.given(
                            new LectureCreatedEvent(lectureId, courseId, 1, List.of(), professorId, LectureStatus.DRAFT),
                            new StudentEnrolledEvent(lectureId, studentToDisenroll),
                            new StudentWaitlistedEvent(lectureId, studentOnWaitlist1, now1),
                            new StudentWaitlistedEvent(lectureId, studentOnWaitlist2, now2)
                    )
                    .when(new DisenrollStudentCommand(lectureId, studentToDisenroll))
                    .expectEvents(
                            new StudentDisenrolledEvent(lectureId, studentToDisenroll),
                            new StudentRemovedFromWaitlistEvent(lectureId, studentOnWaitlist2),
                            new StudentEnrollmentApprovedEvent(lectureId, studentOnWaitlist2, courseId)
                    );
        }

        @Test
        void testDisenrollStudent_shouldDisenrollAndEnrollNext_byWaitlistOrder_ifSameSemester() {
            var studentToDisenroll = UUID.randomUUID();
            var studentOnWaitlist1 = UUID.randomUUID(); // first on waitlist
            var studentOnWaitlist2 = UUID.randomUUID(); // second on waitlist

            var createEvent = new LectureCreatedEvent(lectureId, courseId, 1, List.of(), professorId, LectureStatus.DRAFT);

            var now1 = Instant.now();
            var now2 = now1.plusSeconds(1);

            var student1Lookup = new StudentLookupEntity(studentOnWaitlist1, 5);
            var student2Lookup = new StudentLookupEntity(studentOnWaitlist2, 5);
            when(studentValidator.findByIds(List.of(studentOnWaitlist1, studentOnWaitlist2))).thenReturn(List.of(student1Lookup, student2Lookup));

            fixture.given(
                            createEvent,
                            new StudentEnrolledEvent(lectureId, studentToDisenroll),
                            new StudentWaitlistedEvent(lectureId, studentOnWaitlist1, now1),
                            new StudentWaitlistedEvent(lectureId, studentOnWaitlist2, now2)
                    )
                    .when(new DisenrollStudentCommand(lectureId, studentToDisenroll))
                    .expectEvents(
                            new StudentDisenrolledEvent(lectureId, studentToDisenroll),
                            new StudentRemovedFromWaitlistEvent(lectureId, studentOnWaitlist1),
                            new StudentEnrollmentApprovedEvent(lectureId, studentOnWaitlist1, courseId)
                    );
        }
    }

    private static LectureCreatedEvent lectureCreatedEvent(UUID lectureId, UUID professorId) {
        return new LectureCreatedEvent(lectureId, UUID.randomUUID(), 25, List.of(), professorId, LectureStatus.DRAFT);
    }

    private static LectureCreatedEvent lectureCreatedEvent(UUID lectureId) {
        return lectureCreatedEvent(lectureId, UUID.randomUUID());
    }

    private static TimeSlot anyTimeSlot() {
        return new TimeSlot(LocalDate.of(2025, 12, 1), LocalTime.of(10, 0), LocalTime.of(12, 0));
    }
}
