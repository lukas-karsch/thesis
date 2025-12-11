package karsch.lukas.features.lectures.api;

import java.util.UUID;

public record EnrollmentStatusQuery(UUID lectureId, UUID studentId) {
}
