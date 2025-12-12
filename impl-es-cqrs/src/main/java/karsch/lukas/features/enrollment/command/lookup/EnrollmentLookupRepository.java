package karsch.lukas.features.enrollment.command.lookup;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface EnrollmentLookupRepository extends JpaRepository<EnrollmentLookupEntity, UUID> {
    Optional<EnrollmentLookupEntity> findByLectureIdAndStudentId(UUID lectureId, UUID studentId);
}
