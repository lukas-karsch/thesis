package karsch.lukas.features.lectures.command;

import karsch.lukas.core.uuid.UuidProvider;
import karsch.lukas.features.enrollment.api.EnrollmentCreatedEvent;
import karsch.lukas.features.lectures.api.ConfirmStudentEnrollmentCommand;
import karsch.lukas.features.lectures.api.CreateEnrollmentCommand;
import karsch.lukas.features.lectures.api.StudentEnrollmentApprovedEvent;
import org.axonframework.test.saga.SagaTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EnrollmentApprovalSagaTest {

    private SagaTestFixture<EnrollmentApprovalSaga> fixture;

    private UUID lectureId = UUID.randomUUID();
    private UUID studentId = UUID.randomUUID();
    private UUID enrollmentId = UUID.randomUUID();

    private UuidProvider uuidProvider;

    @BeforeEach
    void setupFixture() {
        fixture = new SagaTestFixture<>(EnrollmentApprovalSaga.class);
        uuidProvider = mock(UuidProvider.class);

        fixture.registerResource(uuidProvider);
    }

    @Test
    void testHandleStudentEnrollmentApprovedEvent() {
        when(uuidProvider.generateUuid()).thenReturn(enrollmentId);

        fixture.givenNoPriorActivity()
                .whenAggregate(lectureId.toString())
                .publishes(new StudentEnrollmentApprovedEvent(lectureId, studentId))
                .expectActiveSagas(1)
                .expectDispatchedCommands(new CreateEnrollmentCommand(enrollmentId, lectureId, studentId));
    }

    @Test
    void testHandleEnrollmentCreatedEvent() {
        fixture.givenAggregate(lectureId.toString())
                .published(
                        new StudentEnrollmentApprovedEvent(lectureId, studentId)
                )
                .whenAggregate(enrollmentId.toString())
                .publishes(new EnrollmentCreatedEvent(enrollmentId, studentId, lectureId))
                .expectActiveSagas(0)
                .expectDispatchedCommands(new ConfirmStudentEnrollmentCommand(lectureId, studentId));
    }
}
