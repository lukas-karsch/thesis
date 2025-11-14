package karsch.lukas.stats;

import java.time.LocalDateTime;

public record GradeChangeDTO(AssessmentType assessmentType, int grade, LocalDateTime changedAt) {
}
