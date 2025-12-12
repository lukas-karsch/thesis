package karsch.lukas.features.lectures.api;

import java.util.UUID;

public record StudentDisenrolledEvent(UUID lectureId, UUID studentId) {
}
