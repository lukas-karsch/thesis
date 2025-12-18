package karsch.lukas.features.stats.queries.gradeHistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
interface EnrollmentProjectionRepository extends JpaRepository<EnrollmentProjectionEntity, UUID> {
    Optional<EnrollmentProjectionEntity> findByStudentIdAndLectureId(UUID studentId, UUID lectureId);
}
