package karsch.lukas.course;

import karsch.lukas.stats.AssessmentType;

public record CourseAssessmentDTO(AssessmentType assessmentType, float weight) {
}
