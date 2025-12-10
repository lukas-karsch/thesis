package karsch.lukas.features.lectures.api;

import java.util.UUID;

public record StudentEnrolledEvent(UUID lectureId, UUID studentId) {
}
