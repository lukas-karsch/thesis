package karsch.lukas.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.Set;
import java.util.UUID;

public record CreateCourseRequest(@NotBlank String name, String description, @Positive int credits,
                                  Set<UUID> prerequisiteCourseIds, @PositiveOrZero int minimumCreditsRequired) {
}
