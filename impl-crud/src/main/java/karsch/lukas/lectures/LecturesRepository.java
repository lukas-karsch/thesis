package karsch.lukas.lectures;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LecturesRepository extends JpaRepository<LectureEntity, Long> {
    @EntityGraph(attributePaths = {"course", "timeSlots", "professor", "waitlist", "enrollments", "course.courseAssessments", "course.prerequisites"})
    Optional<LectureEntity> findDetailsById(Long lectureId);
}
