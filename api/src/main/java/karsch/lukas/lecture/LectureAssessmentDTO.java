package karsch.lukas.lecture;

import karsch.lukas.stats.AssessmentType;

public record LectureAssessmentDTO(SimpleLectureDTO lecture, AssessmentType assessmentType, TimeSlot timeSlot,
                                   float weight) {
}
