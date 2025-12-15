package karsch.lukas.features.enrollment.api;

import java.util.UUID;

public record EnrollmentCreatedEvent(UUID enrollmentId, UUID studentId, UUID lectureId) {
}
