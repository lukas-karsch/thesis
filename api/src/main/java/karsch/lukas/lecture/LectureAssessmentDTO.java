package karsch.lukas.lecture;

import karsch.lukas.stats.AssessmentType;

public record LectureAssessmentDTO(AssessmentType assessmentType, TimeSlot timeSlot) {
}
