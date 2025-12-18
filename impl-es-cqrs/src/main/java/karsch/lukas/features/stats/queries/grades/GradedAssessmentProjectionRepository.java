package karsch.lukas.features.stats.queries.grades;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
interface GradedAssessmentProjectionRepository extends JpaRepository<GradedAssessmentProjectionEntity, GradedAssessmentId> {

    List<GradedAssessmentProjectionEntity> findByLectureIdAndStudentId(UUID lectureId, UUID studentId);
}
