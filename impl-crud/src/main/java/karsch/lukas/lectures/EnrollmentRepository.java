package karsch.lukas.lectures;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, Long> {
    List<EnrollmentEntity> findAllByStudentId(Long studentId);

    Optional<EnrollmentEntity> findByStudentIdAndLectureId(Long studentId, Long lectureId);

    int countByLecture(LectureEntity lecture);

    void deleteByStudentIdAndLectureId(Long studentId, Long lectureId);
}
