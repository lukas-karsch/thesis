package karsch.lukas.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.Set;

public record CreateCourseRequest(@NotBlank String name, String description, @Positive int credits,
                                  Set<Long> prerequisiteCourseIds) {
}
