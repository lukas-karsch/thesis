package karsch.lukas.features.enrollment.api;

import java.util.UUID;

public record UpdateGradeCommand(UUID assessmentId, UUID lectureId, UUID studentId, int grade, UUID professorId) {
    public UpdateGradeCommand {
        if (grade < 0 || grade > 100) {
            throw new IllegalArgumentException("grade must be between 0 and 100");
        }
    }
}

