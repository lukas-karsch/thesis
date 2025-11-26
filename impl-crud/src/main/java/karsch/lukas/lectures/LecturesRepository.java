package karsch.lukas.lectures;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LecturesRepository extends JpaRepository<LectureEntity, Long> {

    @EntityGraph(attributePaths = {"course", "course.prerequisites"})
    Optional<LectureEntity> findWithCourseAndPrerequisitesById(Long id);

    @EntityGraph(attributePaths = {"course", "timeSlots", "professor", "waitlist", "enrollments", "course.prerequisites"})
    Optional<LectureEntity> findDetailsById(Long lectureId);

    @EntityGraph(attributePaths = {"timeSlots", "professor"})
    Optional<LectureEntity> findWithProfessorAndTimeSlotsById(Long lectureId);

    @EntityGraph(attributePaths = {"enrollments", "waitlist"})
    Optional<LectureEntity> findWithEnrollmentsAndWaitlistById(Long lectureId);
}
