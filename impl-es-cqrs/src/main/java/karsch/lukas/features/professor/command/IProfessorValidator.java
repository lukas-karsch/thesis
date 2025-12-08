package karsch.lukas.features.professor.command;

import java.util.UUID;

public interface IProfessorValidator {
    boolean existsById(UUID id);
}
