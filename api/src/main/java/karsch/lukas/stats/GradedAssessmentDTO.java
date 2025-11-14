package karsch.lukas.stats;

import java.time.LocalDateTime;

public record GradedAssessmentDTO(AssessmentType assessmentType, int grade, double weight, LocalDateTime assignedAt) {
}
