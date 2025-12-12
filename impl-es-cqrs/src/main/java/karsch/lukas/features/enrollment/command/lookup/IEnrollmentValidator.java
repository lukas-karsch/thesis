package karsch.lukas.features.enrollment.command.lookup;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IEnrollmentValidator {
    Optional<UUID> getEnrollmentId(UUID lectureId, UUID studentId);

    List<EnrollmentLookupEntity> getEnrollmentsForStudent(UUID studentId);
}
