package karsch.lukas.features.enrollment.api;

import java.util.UUID;

public record EnrollmentCreatedEvent(UUID id, UUID studentId, UUID lectureId) {
}
