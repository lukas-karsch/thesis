package karsch.lukas.lecture;

import karsch.lukas.stats.AssessmentType;
import org.hibernate.validator.constraints.Range;

public record AssignGradeRequest(Long studentId, AssessmentType assessmentType, @Range(min = 0, max = 100) int grade) {
}
