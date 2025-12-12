package karsch.lukas.features.student.command.lookup;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IStudentValidator {
    boolean existsById(UUID id);

    Optional<StudentLookupEntity> findById(UUID id);

    List<StudentLookupEntity> findByIds(List<UUID> ids);
}
