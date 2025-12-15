package karsch.lukas.features.lectures.command;

import karsch.lukas.core.uuid.UuidProvider;
import karsch.lukas.features.enrollment.api.EnrollmentCreatedEvent;
import karsch.lukas.features.lectures.api.ConfirmStudentEnrollmentCommand;
import karsch.lukas.features.lectures.api.CreateEnrollmentCommand;
import karsch.lukas.features.lectures.api.StudentEnrollmentApprovedEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;

import java.util.UUID;

@Saga
@Slf4j
public class EnrollmentApprovalSaga {

    @StartSaga
    @SagaEventHandler(associationProperty = "lectureId")
    public void handle(StudentEnrollmentApprovedEvent event, CommandGateway commandGateway, UuidProvider uuidProvider) {
        log.debug("@StartSaga Handling {}", event);
        UUID enrollmentId = uuidProvider.generateUuid();

        commandGateway.send(
                new CreateEnrollmentCommand(enrollmentId, event.lectureId(), event.studentId())
        );
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "lectureId")
    public void handle(EnrollmentCreatedEvent event, CommandGateway commandGateway) {
        log.debug("@EndSaga Handling {}", event);
        commandGateway.send(new ConfirmStudentEnrollmentCommand(event.lectureId(), event.studentId()));
    }

}
