package karsch.lukas.features.enrollment.api;

import java.util.UUID;

public record GradeUpdatedEvent(UUID id, UUID assessmentId, int grade, UUID professorId) {
}
