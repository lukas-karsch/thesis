package karsch.lukas.features.enrollment;

import karsch.lukas.features.enrollment.api.AwardCreditsCommand;
import karsch.lukas.features.enrollment.api.CreditsAwardedEvent;
import karsch.lukas.features.enrollment.api.EnrollmentCreatedEvent;
import karsch.lukas.features.enrollment.command.AwardCreditsSaga;
import karsch.lukas.features.lectures.api.LectureLifecycleAdvancedEvent;
import karsch.lukas.features.lectures.command.lookup.assessment.IAssessmentValidator;
import karsch.lukas.lecture.LectureStatus;
import karsch.lukas.uuid.UuidUtils;
import org.axonframework.test.saga.SagaTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.mock;

class AwardCreditsSagaTest {

    private SagaTestFixture<AwardCreditsSaga> fixture;

    private IAssessmentValidator assessmentValidator;

    private final UUID enrollmentId = UuidUtils.randomV7();
    private final UUID courseId = UuidUtils.randomV7();
    private final UUID lectureId = UuidUtils.randomV7();
    private final UUID studentId = UuidUtils.randomV7();

    @BeforeEach
    void setupFixture() {
        fixture = new SagaTestFixture<>(AwardCreditsSaga.class);

        assessmentValidator = mock(IAssessmentValidator.class);
        fixture.registerResource(assessmentValidator);
    }

    @Test
    void testStartSaga() {
        fixture.givenNoPriorActivity()
                .whenAggregate(enrollmentId.toString())
                .publishes(new EnrollmentCreatedEvent(enrollmentId, studentId, lectureId, courseId))
                .expectActiveSagas(1);
    }

    @Test
    void testLifecycleFinished() {
        fixture.givenAggregate(enrollmentId.toString())
                .published(new EnrollmentCreatedEvent(enrollmentId, studentId, lectureId, courseId))
                .whenAggregate(lectureId.toString())
                .publishes(new LectureLifecycleAdvancedEvent(lectureId, LectureStatus.FINISHED, UUID.randomUUID()))
                .expectActiveSagas(1)
                .expectDispatchedCommands(new AwardCreditsCommand(enrollmentId, Collections.emptyList()));
    }

    @Test
    void testEndSaga() {
        fixture.givenAggregate(enrollmentId.toString())
                .published(new EnrollmentCreatedEvent(enrollmentId, studentId, lectureId, courseId))
                .andThenAggregate(lectureId.toString())
                .published(new LectureLifecycleAdvancedEvent(lectureId, LectureStatus.FINISHED, UUID.randomUUID()))
                .whenAggregate(enrollmentId.toString())
                .publishes(new CreditsAwardedEvent(enrollmentId, studentId, true, courseId))
                .expectActiveSagas(0);
    }
}
