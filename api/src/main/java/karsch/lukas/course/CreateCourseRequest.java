package karsch.lukas.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateCourseRequest(@NotBlank String name, @NotBlank String description, @Positive int credits) {
}
