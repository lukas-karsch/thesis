package karsch.lukas.lectures;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface LectureAssessmentRepository extends JpaRepository<LectureAssessmentEntity, Long> {
    List<LectureAssessmentEntity> findAllByLecture(LectureEntity lecture);

    List<LectureAssessmentEntity> findAllByLectureIn(Set<LectureEntity> lectureEntities);
}
