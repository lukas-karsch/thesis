package karsch.lukas.lectures;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LecturesRepository extends JpaRepository<LectureEntity, UUID> {

    @EntityGraph(attributePaths = {"course", "course.prerequisites"})
    Optional<LectureEntity> findWithCourseAndPrerequisitesById(UUID id);

    @EntityGraph(attributePaths = {"course", "timeSlots", "professor", "waitlist", "enrollments", "course.prerequisites"})
    Optional<LectureEntity> findDetailsById(UUID lectureId);

    @EntityGraph(attributePaths = {"timeSlots", "professor"})
    Optional<LectureEntity> findWithProfessorAndTimeSlotsById(UUID lectureId);

    @EntityGraph(attributePaths = {"enrollments", "waitlist"})
    Optional<LectureEntity> findWithEnrollmentsAndWaitlistById(UUID lectureId);
}
