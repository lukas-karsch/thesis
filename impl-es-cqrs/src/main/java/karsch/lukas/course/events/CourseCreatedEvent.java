package karsch.lukas.course.events;

import java.util.Set;
import java.util.UUID;

public record CourseCreatedEvent(
        UUID courseId,
        String name,
        String description,
        int credits,
        Set<Long> prerequisiteCourseIds,
        int minimumCreditsRequired
) {
}
