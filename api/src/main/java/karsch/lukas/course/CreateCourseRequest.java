package karsch.lukas.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CreateCourseRequest(@NotBlank String name, @NotBlank String description, @Positive int credits,
                                  List<Long> prerequisiteCourseIds, List<CourseAssessment> assessments) {
    public CreateCourseRequest {
        var totalWeight = assessments.stream().map(CourseAssessment::weight).reduce(Float::sum).orElse(0f);
        if (totalWeight != 1) {
            throw new IllegalArgumentException("Total weight of assessments must sum to 1");
        }
    }
}
