package karsch.lukas.features.stats.queries.gradeHistory;

import karsch.lukas.features.enrollment.api.EnrollmentCreatedEvent;
import karsch.lukas.features.enrollment.api.GradeAssignedEvent;
import karsch.lukas.features.enrollment.api.GradeUpdatedEvent;
import karsch.lukas.features.lectures.api.AssessmentAddedEvent;
import karsch.lukas.features.stats.api.GetGradeHistoryQuery;
import karsch.lukas.stats.GradeHistoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
@ProcessingGroup("gradeHistory")
public class GradeHistoryProjector {

    private final EventStore eventStore;

    private final AssessmentProjectionRepository assessmentProjectionRepository;
    private final EnrollmentProjectionRepository enrollmentProjectionRepository;

    @EventHandler
    public void on(EnrollmentCreatedEvent event) {
        var enrollmentEntity = new EnrollmentProjectionEntity(event.studentId(), event.lectureId(), event.enrollmentId());
        enrollmentProjectionRepository.save(enrollmentEntity);
    }

    @EventHandler
    public void on(AssessmentAddedEvent event) {
        var assessmentEntity = new AssessmentProjectionEntity(event.assessmentId(), event.lectureId());
        assessmentProjectionRepository.save(assessmentEntity);
    }

    @QueryHandler
    public GradeHistoryResponse getGradeHistory(GetGradeHistoryQuery query) {
        UUID lectureId = assessmentProjectionRepository.findById(query.lectureAssessmentId())
                .map(AssessmentProjectionEntity::getLectureId)
                .orElseThrow(() -> new NoSuchElementException("Lecture id not found for assessmentId=" + query.lectureAssessmentId()));

        UUID enrollmentId = enrollmentProjectionRepository.findByStudentIdAndLectureId(query.studentId(), lectureId)
                .map(EnrollmentProjectionEntity::getEnrollmentId)
                .orElseThrow(() -> new NoSuchElementException("Enrollment id not found for studentId=" + query.studentId() + ", lectureId=" + lectureId));

        var stream = eventStore
                .readEvents(enrollmentId.toString())
                .filter(m -> m.getPayload() instanceof GradeAssignedEvent || m.getPayload() instanceof GradeUpdatedEvent);
        stream.forEachRemaining(m -> log.debug("payload={}", m.getPayload()));

        return null;
    }

}
