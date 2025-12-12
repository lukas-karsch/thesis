package karsch.lukas.features.lectures.api;

import java.util.UUID;

public record StudentRemovedFromWaitlistEvent(UUID lectureId, UUID studentId) {
}
