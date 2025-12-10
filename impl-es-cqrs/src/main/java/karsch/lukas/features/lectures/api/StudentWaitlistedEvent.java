package karsch.lukas.features.lectures.api;

import java.time.Instant;
import java.util.UUID;

public record StudentWaitlistedEvent(UUID lectureId, UUID studentId, Instant timestamp) {
}
