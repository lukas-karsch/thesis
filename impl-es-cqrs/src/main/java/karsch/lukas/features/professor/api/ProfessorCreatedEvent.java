package karsch.lukas.features.professor.api;

import java.util.UUID;

public record ProfessorCreatedEvent(UUID id, String firstName, String lastName) {
}
