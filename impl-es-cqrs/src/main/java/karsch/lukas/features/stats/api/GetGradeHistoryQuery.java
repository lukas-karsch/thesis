package karsch.lukas.features.stats.api;

import java.time.LocalDateTime;
import java.util.UUID;

public record GetGradeHistoryQuery(
        UUID studentId,
        UUID lectureAssessmentId,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
