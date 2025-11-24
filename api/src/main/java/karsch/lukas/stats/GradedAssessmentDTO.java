package karsch.lukas.stats;

import java.time.LocalDateTime;

public record GradedAssessmentDTO(Long assessmentId, AssessmentType assessmentType, int grade, double weight,
                                  LocalDateTime assignedAt) {
}
