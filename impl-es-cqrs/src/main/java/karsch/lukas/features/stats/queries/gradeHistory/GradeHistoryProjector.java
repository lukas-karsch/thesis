package karsch.lukas.features.stats.queries.gradeHistory;

import karsch.lukas.features.enrollment.api.EnrollmentCreatedEvent;
import karsch.lukas.features.enrollment.api.GradeAssignedEvent;
import karsch.lukas.features.enrollment.api.GradeUpdatedEvent;
import karsch.lukas.features.lectures.api.AssessmentAddedEvent;
import karsch.lukas.features.stats.api.GetGradeHistoryQuery;
import karsch.lukas.stats.GradeChangeDTO;
import karsch.lukas.stats.GradeHistoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
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
        var enrollmentEntity = new EnrollmentProjectionEntity(event.enrollmentId(), event.studentId(), event.lectureId());
        enrollmentProjectionRepository.save(enrollmentEntity);
    }

    @EventHandler
    public void on(AssessmentAddedEvent event) {
        var assessmentEntity = new AssessmentProjectionEntity(event.assessmentId(), event.lectureId());
        assessmentProjectionRepository.save(assessmentEntity);
    }

    @QueryHandler
    public GradeHistoryResponse getGradeHistory(GetGradeHistoryQuery query) {
        final UUID enrollmentId = getEnrollmentIdFromQuery(query);

        final List<GradeChangeDTO> gradeChanges = eventStore
                .readEvents(enrollmentId.toString())
                .filter(m -> m.getPayload() instanceof GradeAssignedEvent || m.getPayload() instanceof GradeUpdatedEvent)
                .filter(m -> switch (m.getPayload()) {
                    case GradeAssignedEvent gradeAssignedEvent ->
                            gradeAssignedEvent.assessmentId().equals(query.lectureAssessmentId());
                    case GradeUpdatedEvent gradeUpdatedEvent ->
                            gradeUpdatedEvent.assessmentId().equals(query.lectureAssessmentId());
                    default -> false;
                })
                .filter(m -> {
                    log.debug("query={}", query);
                    log.debug("m={}", m);
                    //LocalDateTime eventTimestamp = fromInstant(m.getTimestamp());
                    LocalDateTime eventTimestamp = getEventTimestamp(m);
                    // startDate inclusive
                    boolean afterStartDate = query.startDate() == null || !eventTimestamp.isBefore(query.startDate());
                    // endDate exclusive
                    boolean beforeEndDate = query.endDate() == null || eventTimestamp.isBefore(query.endDate());
                    log.debug("afterStartDate={}, beforeEndDate={}", afterStartDate, beforeEndDate);
                    return afterStartDate && beforeEndDate;
                })
                .asStream()
                .sorted(((m1, m2) -> Long.compare(m2.getSequenceNumber(), m1.getSequenceNumber()))) // reverse
                .map(m -> {
                    // LocalDateTime changedAt = fromInstant(m.getTimestamp());
                    LocalDateTime changedAt = getEventTimestamp(m);
                    return switch (m.getPayload()) {
                        case GradeAssignedEvent gradeAssignedEvent ->
                                new GradeChangeDTO(gradeAssignedEvent.assessmentId(), gradeAssignedEvent.grade(), changedAt);
                        case GradeUpdatedEvent gradeUpdatedEvent ->
                                new GradeChangeDTO(gradeUpdatedEvent.assessmentId(), gradeUpdatedEvent.grade(), changedAt);
                        default ->
                                throw new IllegalStateException("Unexpected event type in grade history stream: " + m.getPayloadType());
                    };
                })
                .toList();

        return new GradeHistoryResponse(
                query.studentId(),
                query.lectureAssessmentId(),
                gradeChanges
        );
    }

    private static LocalDateTime getEventTimestamp(DomainEventMessage<?> m) {
        return switch (m.getPayload()) {
            case GradeAssignedEvent gradeAssignedEvent -> gradeAssignedEvent.assignedAt();
            case GradeUpdatedEvent gradeUpdatedEvent -> gradeUpdatedEvent.assignedAt();
            default -> throw new IllegalStateException();
        };
    }

    private LocalDateTime fromInstant(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));
    }

    private UUID getEnrollmentIdFromQuery(GetGradeHistoryQuery query) {
        final UUID lectureId = assessmentProjectionRepository.findById(query.lectureAssessmentId())
                .map(AssessmentProjectionEntity::getLectureId)
                .orElseThrow(() -> new NoSuchElementException("Lecture id not found for assessmentId=" + query.lectureAssessmentId()));

        return enrollmentProjectionRepository.findByStudentIdAndLectureId(query.studentId(), lectureId)
                .map(EnrollmentProjectionEntity::getEnrollmentId)
                .orElseThrow(() -> new NoSuchElementException("Enrollment id not found for studentId=" + query.studentId() + ", lectureId=" + lectureId));
    }

}
