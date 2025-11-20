package karsch.lukas.lectures;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LectureAssessmentRepository extends JpaRepository<LectureAssessmentEntity, Long> {
}
