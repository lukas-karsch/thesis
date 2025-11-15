package karsch.lukas.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.Set;

public record CreateCourseRequest(@NotBlank String name, String description, @Positive int credits,
                                  Set<Long> prerequisiteCourseIds, Set<CourseAssessmentDTO> assessments) {
    public CreateCourseRequest {
        var totalWeight = assessments.stream().map(CourseAssessmentDTO::weight).reduce(Float::sum).orElse(0f);
        if (totalWeight != 1) {
            throw new IllegalArgumentException("Total weight of assessments must sum to 1");
        }
    }
}
