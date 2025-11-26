package karsch.lukas.lecture;

import jakarta.validation.constraints.NotNull;
import karsch.lukas.stats.AssessmentType;
import org.hibernate.validator.constraints.Range;

public record CreateLectureAssessmentRequest(@NotNull AssessmentType assessmentType,
                                             @NotNull TimeSlot timeSlot,
                                             @Range(min = 0, max = 1) float weight) {
}
