package karsch.lukas.features.lectures.command.lookup;

import java.util.Optional;
import java.util.UUID;

public interface ILectureValidator {
    Optional<LectureLookupEntity> findById(UUID id);
}
