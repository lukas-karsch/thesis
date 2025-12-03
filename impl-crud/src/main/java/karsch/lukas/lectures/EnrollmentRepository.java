package karsch.lukas.lectures;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, Long> {
    List<EnrollmentEntity> findAllByStudentId(UUID studentId);

    @EntityGraph(attributePaths = "lecture.timeSlots")
    List<EnrollmentEntity> findAllWithTimeSlotsByStudentId(UUID studentId);

    Optional<EnrollmentEntity> findByStudentIdAndLectureId(UUID studentId, Long lectureId);

    int countByLecture(LectureEntity lecture);

    boolean existsByStudentIdAndLectureId(UUID studentId, Long lectureId);

    List<EnrollmentEntity> findAllByLecture(LectureEntity lecture);
}
