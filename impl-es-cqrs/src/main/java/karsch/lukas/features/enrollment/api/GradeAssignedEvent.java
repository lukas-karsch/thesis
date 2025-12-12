package karsch.lukas.features.enrollment.api;

import java.util.UUID;

public record GradeAssignedEvent(UUID id, UUID assessmentId, int grade, UUID professorId) {
}
