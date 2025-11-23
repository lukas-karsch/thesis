package karsch.lukas.lectures;

import karsch.lukas.users.StudentEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentGradeRepository extends JpaRepository<AssessmentGradeEntity, Long> {
    Optional<AssessmentGradeEntity> findByStudentAndLectureAssessment(StudentEntity student, LectureAssessmentEntity lectureAssessment);

    @EntityGraph(attributePaths = {"lectureAssessment", "lectureAssessment.lecture"})
    List<AssessmentGradeEntity> findAllByStudent(StudentEntity student);
}
