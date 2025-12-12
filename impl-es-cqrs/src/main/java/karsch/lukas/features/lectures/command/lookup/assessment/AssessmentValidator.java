package karsch.lukas.features.lectures.command.lookup.assessment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class AssessmentValidator implements IAssessmentValidator {
    private final AssessmentLookupRepository assessmentLookupRepository;

    public Optional<AssessmentLookupEntity> findById(UUID id) {
        return assessmentLookupRepository.findById(id);
    }
}
