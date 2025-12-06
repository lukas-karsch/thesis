package karsch.lukas.features.course.api;

import java.util.Set;
import java.util.UUID;

public record CourseCreatedEvent(
        UUID courseId,
        String name,
        String description,
        int credits,
        Set<UUID> prerequisiteCourseIds,
        int minimumCreditsRequired
) {
}

