package karsch.lukas.lecture;

import karsch.lukas.stats.AssessmentType;

public record CreateLectureAssessmentRequest(AssessmentType assessmentType, TimeSlot timeSlot, float weight) {
}
