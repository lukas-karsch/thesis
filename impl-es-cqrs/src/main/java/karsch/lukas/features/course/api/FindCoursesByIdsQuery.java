package karsch.lukas.features.course.api;

import java.util.Set;
import java.util.UUID;

public record FindCoursesByIdsQuery(Set<UUID> courseIds) {
}
