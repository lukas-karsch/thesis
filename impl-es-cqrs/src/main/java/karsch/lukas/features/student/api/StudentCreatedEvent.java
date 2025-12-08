package karsch.lukas.features.student.api;

import java.util.UUID;

public record StudentCreatedEvent(UUID id, String firstName, String lastName) {
}
