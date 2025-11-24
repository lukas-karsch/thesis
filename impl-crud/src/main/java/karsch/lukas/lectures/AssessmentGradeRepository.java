package karsch.lukas.lectures;

import karsch.lukas.users.StudentEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentGradeRepository extends JpaRepository<AssessmentGradeEntity, Long> {
    Optional<AssessmentGradeEntity> findByStudentAndLectureAssessment(StudentEntity student, LectureAssessmentEntity lectureAssessment);

    List<AssessmentGradeEntity> findAllByStudentAndLectureAssessmentIn(StudentEntity student, Collection<LectureAssessmentEntity> lectureAssessments);

    @EntityGraph(attributePaths = {"lectureAssessment", "lectureAssessment.lecture", "lectureAssessment.lecture.course"})
    List<AssessmentGradeEntity> findAllByStudent(StudentEntity student);

    @Query("""
            SELECT COALESCE(SUM(l.course.credits), 0)
            FROM LectureEntity l
            WHERE l.id IN (
                SELECT la.lecture.id
                FROM AssessmentGradeEntity ag
                JOIN ag.lectureAssessment la
                WHERE ag.student = :student
                AND ag.grade >= :failThreshold
                AND la.lecture.lectureStatus >= karsch.lukas.lecture.LectureStatus.FINISHED
                GROUP BY la.lecture.id
                HAVING COUNT(ag.id) = (SELECT COUNT(la2.id) FROM LectureAssessmentEntity la2 WHERE la2.lecture.id = la.lecture.id)
            )
            """)
    int getAccumulatedCreditsForStudent(@Param("student") StudentEntity student, @Param("failThreshold") int failThreshold);
}
