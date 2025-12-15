package karsch.lukas.features.lectures.command.lookup.assessment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
interface AssessmentLookupRepository extends JpaRepository<AssessmentLookupEntity, UUID> {
    List<AssessmentLookupEntity> findAllByLectureId(UUID lectureId);
}
