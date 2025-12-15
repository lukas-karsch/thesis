package karsch.lukas.features.enrollment.command;

import karsch.lukas.features.enrollment.api.AwardCreditsCommand;
import karsch.lukas.features.enrollment.api.CreditsAwardedEvent;
import karsch.lukas.features.enrollment.api.EnrollmentCreatedEvent;
import karsch.lukas.features.lectures.api.LectureLifecycleAdvancedEvent;
import karsch.lukas.features.lectures.command.lookup.assessment.AssessmentLookupEntity;
import karsch.lukas.features.lectures.command.lookup.assessment.IAssessmentValidator;
import karsch.lukas.lecture.LectureStatus;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;

import java.util.List;
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
    public void handle(LectureLifecycleAdvancedEvent event, CommandGateway commandGateway, IAssessmentValidator assessmentValidator) {
        if (event.lectureStatus() != LectureStatus.FINISHED) {
            return;
        }

        List<UUID> allAssessmentIds = assessmentValidator
                .findByLecture(event.lectureId())
                .stream()
                .map(AssessmentLookupEntity::getId)
                .toList();

        commandGateway.send(new AwardCreditsCommand(this.enrollmentId, allAssessmentIds));
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "enrollmentId")
    public void handle(CreditsAwardedEvent event) {
        log.debug("@EndSaga enrollmentId={}", event.enrollmentId());
    }
}
