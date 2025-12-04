package karsch.lukas.lectures;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, UUID> {
    List<EnrollmentEntity> findAllByStudentId(UUID studentId);

    @EntityGraph(attributePaths = "lecture.timeSlots")
    List<EnrollmentEntity> findAllWithTimeSlotsByStudentId(UUID studentId);

    Optional<EnrollmentEntity> findByStudentIdAndLectureId(UUID studentId, UUID lectureId);

    int countByLecture(LectureEntity lecture);

    boolean existsByStudentIdAndLectureId(UUID studentId, UUID lectureId);

    List<EnrollmentEntity> findAllByLecture(LectureEntity lecture);
}
