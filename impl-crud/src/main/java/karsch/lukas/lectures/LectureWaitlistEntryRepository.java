package karsch.lukas.lectures;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LectureWaitlistEntryRepository extends JpaRepository<LectureWaitlistEntryEntity, Long> {
    List<LectureWaitlistEntryEntity> findAllByStudentId(UUID studentId);

    List<LectureWaitlistEntryEntity> findByLectureOrderByCreatedDateAsc(LectureEntity lecture);

}
