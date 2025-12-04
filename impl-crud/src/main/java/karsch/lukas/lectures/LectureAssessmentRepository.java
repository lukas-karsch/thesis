package karsch.lukas.lectures;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface LectureAssessmentRepository extends JpaRepository<LectureAssessmentEntity, UUID> {
    List<LectureAssessmentEntity> findAllByLecture(LectureEntity lecture);

    List<LectureAssessmentEntity> findAllByLectureIn(Set<LectureEntity> lectureEntities);
}
