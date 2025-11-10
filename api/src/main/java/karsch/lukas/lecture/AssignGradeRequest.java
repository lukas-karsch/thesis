package karsch.lukas.lecture;

import org.hibernate.validator.constraints.Range;

public record AssignGradeRequest(Long studentId, @Range(min = 0, max = 100) int grade) {
}
