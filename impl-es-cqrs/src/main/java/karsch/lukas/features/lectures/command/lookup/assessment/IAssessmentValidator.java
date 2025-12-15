package karsch.lukas.features.lectures.command.lookup.assessment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IAssessmentValidator {
    Optional<AssessmentLookupEntity> findById(UUID id);

    List<AssessmentLookupEntity> findByLecture(UUID lectureId);
}
