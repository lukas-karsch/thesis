package karsch.lukas.features.lectures.api;

import java.util.UUID;

public record StudentEnrollmentApprovedEvent(
        UUID lectureId,
        UUID studentId
) {
}
