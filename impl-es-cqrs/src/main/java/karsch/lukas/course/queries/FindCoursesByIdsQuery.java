package karsch.lukas.course.queries;

import java.util.Set;
import java.util.UUID;

public record FindCoursesByIdsQuery(Set<UUID> courseIds) {
}
