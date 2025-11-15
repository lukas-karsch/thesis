package karsch.lukas.courses;

import jakarta.persistence.Embeddable;
import karsch.lukas.stats.AssessmentType;

@Embeddable
public record CourseAssessmentValueObject(AssessmentType assessmentType, float weight) {
}
