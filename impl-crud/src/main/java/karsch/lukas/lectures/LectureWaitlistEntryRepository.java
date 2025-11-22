package karsch.lukas.lectures;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LectureWaitlistEntryRepository extends JpaRepository<LectureWaitlistEntryEntity, Long> {
    List<LectureWaitlistEntryEntity> findAllByStudentId(Long studentId);

    List<LectureWaitlistEntryEntity> findByLectureOrderByCreatedDateAsc(LectureEntity lecture);

}
