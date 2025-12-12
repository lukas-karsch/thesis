package karsch.lukas.features.enrollment.command.lookup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
interface EnrollmentLookupRepository extends JpaRepository<EnrollmentLookupEntity, UUID> {
    Optional<EnrollmentLookupEntity> findByLectureIdAndStudentId(UUID lectureId, UUID studentId);

    List<EnrollmentLookupEntity> findByStudentId(UUID studentId);
}
