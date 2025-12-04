package karsch.lukas.stats;

import java.time.LocalDateTime;
import java.util.UUID;

public record GradedAssessmentDTO(UUID assessmentId, AssessmentType assessmentType, int grade, double weight,
                                  LocalDateTime assignedAt) {
}
