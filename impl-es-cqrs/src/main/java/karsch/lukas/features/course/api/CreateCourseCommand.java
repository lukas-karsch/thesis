package karsch.lukas.features.course.api;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.Set;
import java.util.UUID;

public record CreateCourseCommand(
        @TargetAggregateIdentifier UUID courseId,
        String name,
        String description,
        int credits,
        Set<UUID> prerequisiteCourseIds,
        int minimumCreditsRequired
) {
}
