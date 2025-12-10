package karsch.lukas.features.lectures.api;

import java.util.UUID;

public record WaitlistClearedEvent(UUID lectureId, UUID professorId) {
}

