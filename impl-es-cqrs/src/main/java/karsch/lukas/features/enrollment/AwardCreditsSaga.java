package karsch.lukas.features.enrollment;

import karsch.lukas.features.enrollment.api.AwardCreditsCommand;
import karsch.lukas.features.enrollment.api.CreditsAwardedEvent;
import karsch.lukas.features.enrollment.api.EnrollmentCreatedEvent;
import karsch.lukas.features.lectures.api.LectureLifecycleAdvancedEvent;
import karsch.lukas.lecture.LectureStatus;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;

import java.util.UUID;

@Saga
@Slf4j
public class AwardCreditsSaga {

    private UUID enrollmentId;

    @StartSaga
    @SagaEventHandler(associationProperty = "enrollmentId")
    public void handle(EnrollmentCreatedEvent event) {
        log.debug("@StartSaga enrollmentId={};associating with lectureId={}", event.enrollmentId(), event.lectureId());
        SagaLifecycle.associateWith("lectureId", event.lectureId().toString());
        this.enrollmentId = event.enrollmentId();
    }

    @SagaEventHandler(associationProperty = "lectureId")
    public void handle(LectureLifecycleAdvancedEvent event, CommandGateway commandGateway) {
        if (event.lectureStatus() == LectureStatus.FINISHED) {
            // send command to apply grades
            commandGateway.send(new AwardCreditsCommand(this.enrollmentId));
        }
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "enrollmentId")
    public void handle(CreditsAwardedEvent event) {
        log.debug("@EndSaga enrollmentId={}", event.enrollmentId());
    }
}
