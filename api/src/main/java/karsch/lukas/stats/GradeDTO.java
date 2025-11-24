package karsch.lukas.stats;

import karsch.lukas.lecture.SimpleLectureDTO;

import java.util.List;

public record GradeDTO(int combinedGrade, int credits, SimpleLectureDTO lecture,
                       List<GradedAssessmentDTO> gradedAssessments, boolean isFinalGrade, boolean failed) {
}
