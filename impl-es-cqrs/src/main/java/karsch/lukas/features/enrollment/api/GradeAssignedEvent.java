package karsch.lukas.features.enrollment.api;

import java.time.LocalDateTime;
import java.util.UUID;

public record GradeAssignedEvent(UUID enrollmentId, UUID assessmentId, int grade, UUID professorId, UUID studentId,
                                 LocalDateTime assignedAt) {
}
