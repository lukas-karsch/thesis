package karsch.lukas.course.queries;

import java.util.Set;

public record FindCoursesByIdsQuery(Set<Long> courseIds) {
}
