package karsch.lukas.features.enrollment.command.lookup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class EnrollmentValidator implements IEnrollmentValidator {
    private final EnrollmentLookupRepository enrollmentLookupRepository;

    @Override
    public Optional<UUID> getEnrollmentId(UUID lectureId, UUID studentId) {
        return enrollmentLookupRepository.findByLectureIdAndStudentId(lectureId, studentId)
                .map(EnrollmentLookupEntity::getId);
    }
}
