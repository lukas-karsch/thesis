package karsch.lukas.features.enrollment.api;

import java.util.UUID;

public record CreditsAwardedEvent(UUID enrollmentId, UUID lectureId, UUID studentId) {
}
