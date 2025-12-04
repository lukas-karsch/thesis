package karsch.lukas.lecture;

import org.hibernate.validator.constraints.Range;

import java.util.UUID;

public record AssignGradeRequest(UUID studentId, UUID assessmentId, @Range(min = 0, max = 100) int grade) {
}
