package karsch.lukas.features.student.api;

import java.util.UUID;

public record StudentCreatedEvent(UUID studentId, String firstName, String lastName, int semester) {
}
