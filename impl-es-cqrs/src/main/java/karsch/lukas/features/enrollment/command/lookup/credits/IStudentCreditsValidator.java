package karsch.lukas.features.enrollment.command.lookup.credits;

import java.util.UUID;

public interface IStudentCreditsValidator {
    boolean hasEnoughCreditsToEnroll(UUID studentId, UUID courseId);

    boolean hasPassedAllPrerequisites(UUID studentId, UUID courseId);
}
