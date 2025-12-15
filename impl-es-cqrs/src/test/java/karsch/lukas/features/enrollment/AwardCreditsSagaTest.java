package karsch.lukas.features.enrollment;

import karsch.lukas.features.enrollment.api.AwardCreditsCommand;
import karsch.lukas.features.enrollment.api.CreditsAwardedEvent;
import karsch.lukas.features.enrollment.api.EnrollmentCreatedEvent;
import karsch.lukas.features.lectures.api.LectureLifecycleAdvancedEvent;
import karsch.lukas.lecture.LectureStatus;
import karsch.lukas.uuid.UuidUtils;
import org.axonframework.test.saga.SagaTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class AwardCreditsSagaTest {

    private SagaTestFixture<AwardCreditsSaga> fixture;

    private final UUID enrollmentId = UuidUtils.randomV7();
    private final UUID lectureId = UuidUtils.randomV7();
    private final UUID studentId = UuidUtils.randomV7();

    @BeforeEach
    void setupFixture() {
        fixture = new SagaTestFixture<>(AwardCreditsSaga.class);
    }

    @Test
    void testStartSaga() {
        fixture.givenNoPriorActivity()
                .whenAggregate(enrollmentId.toString())
                .publishes(new EnrollmentCreatedEvent(enrollmentId, studentId, lectureId))
                .expectActiveSagas(1);
    }

    @Test
    void testLifecycleFinished() {
        fixture.givenAggregate(enrollmentId.toString())
                .published(new EnrollmentCreatedEvent(enrollmentId, studentId, lectureId))
                .whenAggregate(lectureId.toString())
                .publishes(new LectureLifecycleAdvancedEvent(lectureId, LectureStatus.FINISHED, UUID.randomUUID()))
                .expectActiveSagas(1)
                .expectDispatchedCommands(new AwardCreditsCommand(enrollmentId));
    }

    @Test
    void testEndSaga() {
        fixture.givenAggregate(enrollmentId.toString())
                .published(new EnrollmentCreatedEvent(enrollmentId, studentId, lectureId))
                .andThenAggregate(lectureId.toString())
                .published(new LectureLifecycleAdvancedEvent(lectureId, LectureStatus.FINISHED, UUID.randomUUID()))
                .whenAggregate(enrollmentId.toString())
                .publishes(new CreditsAwardedEvent(enrollmentId, lectureId, studentId))
                .expectActiveSagas(0);
    }
}
