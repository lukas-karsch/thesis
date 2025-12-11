package karsch.lukas.features.lectures.queries;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StudentLecturesRepository extends JpaRepository<StudentLecturesProjectionEntity, UUID> {

    @Query(
            value = "SELECT * FROM student_lectures_projection WHERE :lectureId = ANY(waitlisted_ids)",
            nativeQuery = true
    )
    List<StudentLecturesProjectionEntity> findByLectureIdInWaitlist(@Param("lectureId") UUID lectureId);
}
